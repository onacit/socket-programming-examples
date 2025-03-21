package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp3Client_SocketChannel_NonBlocking extends Rfc863Tcp$Client {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var client = SocketChannel.open()) { // IOException
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert client.isBlocking();
            client.configureBlocking(false);
            assert !client.isBlocking();
            // --------------------------------------------- try to connect, and register the <client> to the <selector>
            final SelectionKey clientKey;
            if (client.connect(_Constants.SERVER_ENDPOINT)) { // connected, immediately
                log.debug("connected to {}, through {}",
                          client.getRemoteAddress(), // IOException
                          client.getLocalAddress() // IOException
                );
                clientKey = client.register(selector, SelectionKey.OP_WRITE); // ClosedChannelException
            } else { // not, immediately, connected
                clientKey = client.register(selector, SelectionKey.OP_CONNECT); // ClosedChannelException
            }
            // ------------------------------------------ read 'quit', cancel the <clientKey>, and wakeup the <selector>
            __Utils.readQuitAndCall(true, () -> {
                clientKey.cancel();
                assert !clientKey.isValid();
                selector.wakeup();
                return null;
            });
            // ------------------------------------------------------------------------------- keep sending random bytes
            for (final var src = ByteBuffer.allocate(1); clientKey.isValid(); ) {
                // ---------------------------------------------------------------------------------------------- select
                final var count = selector.select(0L); // IOException
                assert count >= 0; // why not 1?
                // -------------------------------------------------------------------------------- handle selected keys
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    // ---------------------------------------------------------------------------------- finish connect
                    if (key.isConnectable()) {
                        final var finished = client.finishConnect();
                        assert finished;
                        log.debug("connected to {}, through {}",
                                  client.getRemoteAddress(), // IOException
                                  client.getLocalAddress() // IOException
                        );
                        // ---------------------------------------------------------------------------- unset OP_CONNECT
                        key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                        // -------------------------------------------------------------------------------- set OP_WRITE
                        key.interestOpsOr(SelectionKey.OP_WRITE);
                        // ----------------------- if <THROTTLE>, periodically set OP_WRITE, and wake up the< selector>
                        if (_Constants.THROTTLE) {
                            Thread.ofVirtual().start(() -> {
                                assert Thread.currentThread().isDaemon();
                                while (!Thread.currentThread().isInterrupted() && key.isValid()) {
                                    try {
                                        Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                                    } catch (final InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        key.cancel();
                                        selector.wakeup();
                                    }
                                    key.interestOps(SelectionKey.OP_WRITE);
                                    selector.wakeup();
                                }
                            });
                        }
                    }
                    // ------------------------------------------------------------------------------------------- write
                    if (key.isWritable()) {
                        __Utils.randomize(src);
                        final var w = client.write(src); // IOException
                        assert w > 0;
                        // --------------------------------------------------------------- if <THROTTLE>, unset OP_WRITE
                        if (_Constants.THROTTLE) {
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                        }
                    }
                }
            }
        }
    }
}
