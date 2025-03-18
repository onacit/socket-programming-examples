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
        try (var selector = Selector.open();
             var server = ServerSocketChannel.open()) {
            // reuse address/port --------------------------------------------------------------------------------------
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
            // bind ----------------------------------------------------------------------------------------------------
            assert !server.socket().isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            assert server.socket().isBound();
            log.info("bound to {}", server.getLocalAddress());
            // configure non-blocking ----------------------------------------------------------------------------------
            assert server.isBlocking();
            server.configureBlocking(false); // IOException
            assert !server.isBlocking();
            // register server to the selector --------------------------------------------------------------------------------
            final var serverKey = server.register(selector, SelectionKey.OP_ACCEPT); // ClosedChannelException
            // read 'quit', cancel the serverKey, and wakeup the selector ----------------------------------------------
            __Utils.readQuitAndRun(true, () -> {
                serverKey.cancel();
                assert !serverKey.isValid();
                selector.wakeup();
            });
            // keep selecting and processing selected keys -------------------------------------------------------------
            for (final var dst = ByteBuffer.allocate(1); serverKey.isValid(); ) {
                // select ----------------------------------------------------------------------------------------------
                final var count = selector.select(0L); // IOException
                assert count >= 0;
                // process selected keys -------------------------------------------------------------------------------
                for (var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    final var channel = key.channel();
                    // accept ------------------------------------------------------------------------------------------
                    if (key.isAcceptable()) {
                        assert channel == server;
                        // accept --------------------------------------------------------------------------------------
                        final var client = ((ServerSocketChannel) channel).accept(); // IOException
                        log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                        // shutdown input ------------------------------------------------------------------------------
                        client.shutdownInput(); // IOException
                        // configure non-blocking ----------------------------------------------------------------------
                        assert client.isBlocking();
                        client.configureBlocking(false); // IOException
                        assert !client.isBlocking();
                        // register client to the selector -------------------------------------------------------------
                        final var clientKey = client.register(selector, 0); // ClosedChannelException
                        // attach a new pattern generator to the client key --------------------------------------------
                        clientKey.attach(_Utils.newPatternGenerator());
                        // start a new thread keep setting OP_WRITE on the client key ----------------------------------
                        Thread.ofVirtual().start(() -> {
                            while (clientKey.isValid()) {
                                clientKey.interestOpsOr(SelectionKey.OP_WRITE);
                                selector.wakeup();
                                try {
                                    Thread.sleep(ThreadLocalRandom.current().nextInt(128));
                                } catch (final InterruptedException ie) {
                                    log.error("interrupted while sleeping", ie);
                                    Thread.currentThread().interrupt();
                                    clientKey.cancel();
                                    assert !clientKey.isValid();
                                }
                            }
                        });
                    }
                    // write -------------------------------------------------------------------------------------------
                    if (key.isWritable()) {
                        assert channel instanceof SocketChannel;
                        final var generator = (_Generator) key.attachment();
                        try {
                            ((WritableByteChannel) channel).write(generator.buffer()); // IOException
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            assert key.isWritable(); // still
                        } catch (final IOException ioe) {
                            try {
                                channel.close(); // IOException
                            } catch (final IOException ioe2) {
                                // empty
                            }
                            key.cancel();
                            assert !key.isValid();
                        }
                    }
                }
            }
        }
    }
}
