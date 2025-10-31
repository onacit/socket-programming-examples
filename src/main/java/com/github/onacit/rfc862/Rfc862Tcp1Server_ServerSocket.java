package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;

/**
 * A minimal TCP server that discards bytes received from clients.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Rfc862Tcp1Server_ServerSocket extends Rfc862Tcp$Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = new ServerSocket()) { // IOException
            // -------------------------------------------------------------------------------------------- SO_REUSEADDR
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uoe) {
                // empty
            }
            // -------------------------------------------------------------------------------------------- SO_REUSEPORT
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uoe) {
                // empty
            }
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.isBound();
            final var endpoint = __Utils.parseSocketAddress(_Constants.PORT, args)
                    .orElse(_Constants.SERVER_ENDPOINT_TO_BIND);
            server.bind(endpoint);
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            // --------------------------------------------------------------------- read 'quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // ------------------------------------------------------------------ keep accepting, receiving, and sending
            while (!server.isClosed()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteSocketAddress());
                        for (int b; (b = client.getInputStream().read()) != -1 && !server.isClosed(); ) { // IOException
                            client.getOutputStream().write(b); // IOException
                            client.getOutputStream().flush(); // IOException
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
