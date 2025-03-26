package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp4Server_ServerSocketChannel_NonBlocking {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var server = ServerSocketChannel.open()) { // IOException
            // ------------------------------------------------------------------------------- try to reuse address/port
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            } catch (final Exception e) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, e);
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
            } catch (final Exception e) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, e);
            }
            server.socket().setReuseAddress(true);
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.socket().isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            assert server.socket().isBound();
            log.info("bound to {}", server.getLocalAddress());
            // ---------------------------------------------------------------------- configure <server> as non-blocking
            assert server.isBlocking();
            server.configureBlocking(false); // IOException
            assert !server.isBlocking();
            // --------------------------------------------------------------------- register <server> to the <selector>
            assert !server.isRegistered();
            final var serverKey = server.register(selector, SelectionKey.OP_ACCEPT); // ClosedChannelException
            assert server.isRegistered();
            // ----------------------- read 'quit', close all clients, cancel the <serverKey>, and wakeup the <selector>
            __Utils.readQuitAndRun(true, () -> {
                // closes all client channels
                selector.keys().stream().filter(k -> k.channel() instanceof SocketChannel).forEach(k -> {
                    try {
                        k.channel().close();
                        assert !k.isValid();
                    } catch (IOException ioe) {
                        log.error("failed to close {}", k.channel());
                        k.cancel();
                        assert !k.isValid();
                    }
                });
                // cancel the <serverKey>
                serverKey.cancel();
                assert !serverKey.isValid();
                // wake up the <selector>
                selector.wakeup();
            });
            // ------------------------------------------------------------- keep selecting and processing selected keys
            for (final var dst = ByteBuffer.allocate(1); serverKey.isValid(); ) {
                // ---------------------------------------------------------------------------------------------- select
                final var count = selector.select(0L); // IOException
                assert count >= 0;
                // ------------------------------------------------------------------------------- process selected keys
                for (var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    final var channel = key.channel();
                    // -------------------------------------------------------------------------------------- acceptable
                    if (key.isAcceptable()) {
                        assert channel == server;
                        // -------------------------------------------------------------------------------------- accept
                        final var client = ((ServerSocketChannel) channel).accept(); // IOException
                        log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                        // --------------------------------------------------------------------shutdown input (optional)
                        client.shutdownInput(); // IOException
                        // ---------------------------------------------------------- configure <client> as non-blocking
                        assert client.isBlocking();
                        client.configureBlocking(false); // IOException
                        assert !client.isBlocking();
                        // -------------------------------------------- register client to the <selector> for <OP_WRITE>
                        final var clientKey = client.register(selector, SelectionKey.OP_WRITE
                        ); // ClosedChannelException
                        // --------------------------------------------attach a new pattern generator to the <clientKey>
                        clientKey.attach(_Utils.newPatternGenerator());
                        // ------------------------------- start a new thread keep setting <OP_WRITE> on the <clientKey>
                        Thread.ofVirtual().start(() -> {
                            while (clientKey.isValid()) {
                                try {
                                    Thread.sleep(ThreadLocalRandom.current().nextInt(128));
                                } catch (final InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    log.error("interrupted while sleeping", ie);
                                    clientKey.cancel();
                                    assert !clientKey.isValid();
                                    return;
                                }
                                clientKey.interestOpsOr(SelectionKey.OP_WRITE);
                                selector.wakeup();
                            }
                        });
                    }
                    // ---------------------------------------------------------------------------------------- writable
                    if (key.isWritable()) {
                        assert channel instanceof SocketChannel;
                        final var generator = (_Generator) key.attachment();
                        try {
                            ((WritableByteChannel) channel).write(generator.buffer()); // IOException
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            assert key.isWritable(); // still
                        } catch (final IOException ioe) {
                            log.error("failed to write through {}", channel, ioe);
                            try {
                                channel.close(); // IOException
                                assert !key.isValid();
                            } catch (final IOException ioe2) {
                                log.error("failed to close {}", channel, ioe2);
                                key.cancel();
                                assert !key.isValid();
                            }
                        }
                    }
                }
            }
        }
    }
}
