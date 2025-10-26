package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;

@Slf4j
class Rfc863Tcp3Server_ServerSocketChannel_NonBlocking extends Rfc863Tcp$Server {

    /**
     * .
     *
     * @param args an array of command line arguments.
     * @throws IOException if an I/O error occurs.
     * @see <a
     * href="https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/ServerSocketChannel.html">java.nio.channels.ServerSocketChannel</a>
     */
    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var server = ServerSocketChannel.open()) { // IOException
            assert !server.socket().isBound();
            // ------------------------------------------------------------------------------- try to reuse address/port
            {
                if (false) {
                    server.socket().setReuseAddress(true); // SocketException // -> setOption(SO_REUSEADDR, TRUE)
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uoe);
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uoe);
                }
            }
            // ---------------------------------------------------------------------------------------------------- bind
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            assert server.socket().isBound();
            log.info("bound to {}", server.getLocalAddress());
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert server.isBlocking();
            server.configureBlocking(false); // IOException
            assert !server.isBlocking();
            // ----------------------------------------------------------------- register the <server> to the <selector>
            final var serverKey = server.register(selector, SelectionKey.OP_ACCEPT); // ClosedChannelException
            // -------------------------- read '!quit', cancel the <serverKey>, close all clients, wake up the <selector>
            __Utils.readQuitAndRun(true, () -> {
                serverKey.cancel();
                assert !serverKey.isValid();
                selector.keys().stream()
                        .map(SelectionKey::channel)
                        .filter(SocketChannel.class::isInstance)
                        .forEach(c -> {
                            try {
                                c.close();
                                assert !c.isOpen();
                            } catch (final IOException ioe) {
                                throw new RuntimeException("failed to close " + c, ioe);
                            }
                        });
                selector.wakeup();
            });
            // ------------------------------------------------------------------ keep selecting, handling selected keys
            final var dst = ByteBuffer.allocate(1);
            assert dst.capacity() > 0;
            while (serverKey.isValid()) {
                final var count = selector.select(0); // IOException
                assert count >= 0;
                for (var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    final var channel = key.channel();
                    // ------------------------------------------------------------------------------------------ accept
                    if (key.isAcceptable()) {
                        assert channel == server;
                        final var client = ((ServerSocketChannel) channel).accept(); // IOException
                        final var address = client.getRemoteAddress(); // IOException
                        log.debug("accepted from {}, through {}",
                                  address,
                                  client.getLocalAddress() // IOException
                        );
                        // ---------------------------------------------------------------- shutdown output (optionally)
                        if (_Constants.TCP_SERVER_SHUTDOWN_CLIENT_OUTPUT) {
                            client.shutdownOutput(); // IOException
                            try {
                                client.write(ByteBuffer.allocate(0));
                                assert false;
                            } catch (final IOException ioe) {
                            }
                            try {
                                client.write(ByteBuffer.allocate(1));
                                assert false;
                            } catch (final IOException ioe) {
                            }
                        }
                        // ---------------------------------------------------------------------- configure non-blocking
                        assert client.isBlocking();
                        client.configureBlocking(false); // IOException
                        assert !client.isBlocking();
                        // ----------------------------------- register the <client> to the <selector> for the <OP_READ>
                        final var clientKey = client.register(selector, SelectionKey.OP_READ); // ClosedChannelException
                        assert !clientKey.isReadable();
                        // ------------------------------------------------------------------ attach the <address>
                        clientKey.attach(address);
                    }
                    // -------------------------------------------------------------------------------------------- read
                    if (key.isReadable()) {
                        assert channel instanceof SocketChannel;
                        final var attachment = key.attachment();
                        assert attachment instanceof SocketAddress;
                        final var r = ((ReadableByteChannel) channel).read(dst.clear()); // IOException
                        if (r == -1) {
                            channel.close(); // IOException
                            assert !key.isValid();
                            continue;
                        }
                        assert r > 0;
                        for (dst.flip(); dst.hasRemaining(); ) {
                            // TODO: use __Utils#formatOctet(int) 
                            log.debug("discarding {}, received from {}", String.format("0x%02X", dst.get()),
                                      attachment);
                        }
                    }
                }
            }
        }
    }
}
