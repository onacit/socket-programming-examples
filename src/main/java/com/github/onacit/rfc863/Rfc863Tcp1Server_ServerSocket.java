package com.github.onacit.rfc863;

import com.github.onacit.__SocketUtils;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
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
     * @see <a
     * href="https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/ServerSocket.html">java.net.ServerSocket</a>
     */
    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = new ServerSocket()) { // IOException
            assert !server.isBound();
            // ----------------------------------------------------------------------------- SO_REUSEADDR / SO_REUSEPORT
            __SocketUtils.SO_REUSEADDR(server); // IOException
            __SocketUtils.SO_REUSEPORT(server); // IOException
            // ---------------------------------------------------------------------------------------------------- bind
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND, _Constants.TCP_SERVER_BACKLOG); // IOException
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            // -------------------------------------------------------------------- read '!quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // ------------------------------------------------------------------------------------------ keep accepting
            while (!server.isClosed()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        final var address = client.getRemoteSocketAddress();
                        log.debug("accepted from {}", address);
                        // ------------------------------------------------------------------ shutdown output (optional)
                        if (_Constants.TCP_SERVER_SHUTDOWN_CLIENT_OUTPUT) {
                            log.debug("shutting down the output...");
                            client.shutdownOutput(); // IOException
                        }
                        // ------------------------------------------------------------------------------ keep receiving
                        final var input = client.getInputStream(); // IOException
                        while (!server.isClosed()) { // IOException
                            final var b = input.read();
                            if (b == -1) {
                                break;
                            }
                            _Utils.logDiscarding(b, address);
                        }
                    } finally {
                        client.close(); // IOException
                    }
                    return null;
                });
            }
        }
    }
}
