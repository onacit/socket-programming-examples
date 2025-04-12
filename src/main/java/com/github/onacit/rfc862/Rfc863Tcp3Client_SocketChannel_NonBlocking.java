package com.github.onacit.rfc862;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp3Client_SocketChannel_NonBlocking extends Rfc862Tcp$Client {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var client = SocketChannel.open()) { // IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (_Constants.TCP_CLIENT_BIND) {
                assert !client.socket().isBound();
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0));
                assert client.socket().isBound();
                log.debug("bound to {}", client.getLocalAddress());
            }
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert client.isBlocking();
            client.configureBlocking(false);
            assert !client.isBlocking();
            // --------------------------------------------- try to connect, and register the <client> to the <selector>
            final SelectionKey clientKey;
            if (client.connect(_Constants.SERVER_ENDPOINT)) { // IOException
                log.debug("connected, immediately, to {}, through {}",
                          client.getRemoteAddress(), // IOException
                          client.getLocalAddress() // IOException
                );
                // --------------------------------------------------------------------------- shutdown input (optional)
                if (_Constants.TCP_CLIENT_SHUTDOWN_INPUT) {
                    client.shutdownInput(); // IOException
                    final var r = client.read(ByteBuffer.allocate(ThreadLocalRandom.current().nextInt(2)));
                    assert r == -1 : "expected; as the input has been shut down";
                }
                // -------------------------------------------------- register <client> to the <selector> for <OP_WRITE>
                clientKey = client.register(selector, SelectionKey.OP_WRITE); // ClosedChannelException
            } else {
                log.debug("not, immediately, connected");
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
                        final var finished = client.finishConnect(); // IOException
                        assert finished; // why?
                        log.debug("connected to {}, through {}",
                                  client.getRemoteAddress(), // IOException
                                  client.getLocalAddress() // IOException
                        );
                        // ------------------------------------------------------------------- shutdown input (optional)
                        if (_Constants.TCP_CLIENT_SHUTDOWN_INPUT) {
                            client.shutdownInput(); // IOException
                            final var r = client.read(ByteBuffer.allocate(1));
                            assert r == -1 : "expected; as the input has been shut down";
                        }
                        // -------------------------------------------------------------------------- unset <OP_CONNECT>
                        key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                        // ------------------------------------------------------------------------------ set <OP_WRITE>
                        key.interestOpsOr(SelectionKey.OP_WRITE);
                    }
                    // ------------------------------------------------------------------------------------------- write
                    if (key.isWritable()) {
                        __Utils.randomizeAvailableAndContent(src);
                        final var w = client.write(src); // IOException
                        assert w >= 0;
                        // --------------------------------------------------------------- if <THROTTLE>, unset OP_WRITE
                        if (_Constants.THROTTLE) {
                            // just for the sanity
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            Thread.ofVirtual().start(() -> {
                                assert Thread.currentThread().isDaemon();
                                try {
                                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                                    key.interestOps(SelectionKey.OP_WRITE);
                                    selector.wakeup();
                                } catch (final InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    key.cancel();
                                    assert !key.isValid();
                                    selector.wakeup();
                                }
                            });
                        }
                        continue;
                    }
                    // -------------------------------------------------------------------------------------------- read
                    if (key.isReadable()) {
                        assert false : "never registered for <OP_READ>";
                        continue;
                    }
                }
            }
        }
    }
}
