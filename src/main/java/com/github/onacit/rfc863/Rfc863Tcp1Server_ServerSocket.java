package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;

/**
 * A minimal TCP server that discards bytes received from clients.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
class Rfc863Tcp1Server_ServerSocket extends Rfc863Tcp$Server {

    /**
     * .
     *
     * @param args an array of command line arguments.
     * @throws IOException if an I/O error occurs.
     */
    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = new ServerSocket()) { // IOException
            // -------------------------------------------------------------------------------------- reuse address/port
            if (false) {
                try {
                    server.setReuseAddress(true);
                } catch (final SocketException se) {
                    log.error("failed to set reuseAddress", se);
                }
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                log.debug("set {}", StandardSocketOptions.SO_REUSEADDR);
            } catch (final UnsupportedOperationException uoe) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uoe);
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                log.debug("set {}", StandardSocketOptions.SO_REUSEPORT);
            } catch (final UnsupportedOperationException uoe) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uoe);
            }
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND, _Constants.TCP_SERVER_BACKLOG); // IOException
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            // --------------------------------------------------------------------- read 'quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // ------------------------------------------------------------------------------------------ keep accepting
            while (!server.isClosed()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        final var remoteAddress = client.getRemoteSocketAddress();
                        log.debug("accepted from {}", remoteAddress);
                        // ------------------------------------------------------------------ shutdown output (optional)
                        if (_Constants.TCP_SERVER_SHUTDOWN_CLIENT_OUTPUT) {
                            client.shutdownOutput(); // IOException
                            try {
                                client.getOutputStream().write(0); // IOException
                                assert false : "shouldn't be here";
                            } catch (final IOException ioe) {
                                log.debug("expected; as the output has been shut down", ioe);
                            }
                        }
                        // ------------------------------------------------------------------------------ keep receiving
                        for (int b; (b = client.getInputStream().read()) != -1 && !server.isClosed(); ) { // IOException
                            log.debug("discarding {} received from {}", String.format("0x%02X", b), remoteAddress);
                        }
                    } finally {
                        client.close(); // IOException
                    }
                    return null;
                });
            } // end-of-while
        } // end-of-try-with-resources
    } // end-of-main
}
