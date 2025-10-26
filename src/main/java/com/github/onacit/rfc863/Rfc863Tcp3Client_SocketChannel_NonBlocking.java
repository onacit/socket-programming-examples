package com.github.onacit.rfc863;

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
class Rfc863Tcp3Client_SocketChannel_NonBlocking extends Rfc863Tcp$Client {

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
                    {
                        final var r = client.read(ByteBuffer.allocate(0)); // IOException
                        assert r == -1;
                    }
                    {
                        final var r = client.read(ByteBuffer.allocate(1)); // IOException
                        assert r == -1;
                    }
                }
                // -------------------------------------------------- register <client> to the <selector> for <OP_WRITE>
                clientKey = client.register(selector, SelectionKey.OP_WRITE); // ClosedChannelException
            } else {
                log.debug("not, immediately, connected");
                clientKey = client.register(selector, SelectionKey.OP_CONNECT); // ClosedChannelException
            }
            // ---------------------------------------- read '!quit', cancel the <clientKey>, and wake up the <selector>
            __Utils.readQuitAndCall(true, () -> {
                clientKey.cancel();
                assert !clientKey.isValid();
                selector.wakeup();
                return null;
            });
            // ------------------------------------------------------------------------------- keep sending random bytes
            final var src = ByteBuffer.allocate(1);
            assert src.capacity() > 0;
            while (clientKey.isValid()) {
                // ---------------------------------------------------------------------------------------------- select
                final var count = selector.select(0L); // IOException
                assert count >= 0; // why not just one?
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
                            {
                                final var r = client.read(ByteBuffer.allocate(0)); // IOException
                                assert r == -1;
                            }
                            {
                                final var r = client.read(ByteBuffer.allocate(1)); // IOException
                                assert r == -1;
                            }
                        }
                        // -------------------------------------------------------------------------- unset <OP_CONNECT>
                        key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                        // ------------------------------------------------------------------------------ set <OP_WRITE>
                        key.interestOpsOr(SelectionKey.OP_WRITE);
                        assert !key.isWritable();
                    }
                    // ------------------------------------------------------------------------------------------- write
                    if (key.isWritable()) {
                        {
                            __Utils.randomizeAvailableAndContent(src);
                            assert src.hasRemaining();
                        }
                        final var w = client.write(src); // IOException
                        assert w >= 0; // hmm...
                        // --------------------------------------------------------------- if <THROTTLE>, unset OP_WRITE
                        if (_Constants.TCP_CLIENT_THROTTLE) {
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            Thread.ofVirtual().start(() -> {
                                assert Thread.currentThread().isDaemon();
                                try {
                                    Thread.sleep(
                                            ThreadLocalRandom.current().nextLong(1024L) + 1024L
                                    ); // InterruptedException
                                    key.interestOps(SelectionKey.OP_WRITE);
                                } catch (final InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    key.cancel();
                                    assert !key.isValid();
                                }
                                selector.wakeup();
                            });
                        }
                        continue;
                    }
                    // ------------------------------------------------------------------------------------------- read?
                    if (key.isReadable()) {
                        assert false : "never has been registered for <OP_READ>";
                        continue;
                    }
                }
            }
        }
    }
}
