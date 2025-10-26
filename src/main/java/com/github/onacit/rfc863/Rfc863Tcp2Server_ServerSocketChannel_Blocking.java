package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp2Server_ServerSocketChannel_Blocking extends Rfc863Tcp$Server {

    /**
     * .
     *
     * @param args an array of command line arguments.
     * @throws IOException if an I/O error occurs.
     * @see <a
     * href="https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/ServerSocketChannel.html">java.nio.channels.ServerSocketChannel</a>
     */
    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = ServerSocketChannel.open()) { // IOException
            assert !server.socket().isBound();
            // ------------------------------------------------------------------------------- try to reuse address/port
            {
                if (false) {
                    server.socket().setReuseAddress(true);
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
            // -------------------------------------------------------------------- read '!quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // ------------------------------------------- keep accepting clients and start a new thread for each client
            assert server.isBlocking(); // !!!
            while (server.isOpen()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        final var address = client.getRemoteAddress(); // IOException
                        log.debug("accepted from {}", address);
                        // ------------------------------------------------------------------ shutdown output (optional)
                        if (_Constants.TCP_SERVER_SHUTDOWN_CLIENT_OUTPUT) {
                            client.shutdownOutput(); // IOException
                            {
                                try {
                                    client.write(ByteBuffer.allocate(0)); // IOException
                                    assert false : "shouldn't be here";
                                } catch (final IOException ioe) {
                                }
                                try {
                                    client.write(ByteBuffer.allocate(1)); // IOException
                                    assert false : "shouldn't be here";
                                } catch (final IOException ioe) {
                                }
                            }
                        }
                        // ------------------------------------------------------------------------------ keep receiving
                        final var dst = ByteBuffer.allocate(1);
                        assert dst.capacity() > 0;
                        while (server.isOpen()) {
                            final var r = client.read(dst.clear()); // IOException
                            if (r == -1) {
                                break;
                            }
                            assert r > 0;
                            for (dst.flip(); dst.hasRemaining(); ) {
                                // TODO: use __Utils.formatOctet(octet) method
                                log.debug("discarding {}, received from {}", String.format("0x%1$02X", dst.get()),
                                          address);
                            }
                        }
                    } finally {
                        client.close(); // IOException
                        assert !client.isOpen();
                        assert client.socket().isClosed();
                    }
                    return null;
                });
            }
        }
    }
}
