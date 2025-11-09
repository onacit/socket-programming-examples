package com.github.onacit.rfc862;

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
class Rfc862Tcp1Server_ServerSocket extends Rfc862Tcp$Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = new ServerSocket()) { // IOException
            assert !server.isBound();
            // ----------------------------------------------------------------------------- SO_REUSEADDR / SO_REUSEPORT
            __SocketUtils.SO_REUSEADDR(server); // IOException
            __SocketUtils.SO_REUSEPORT(server); // IOException
            // ---------------------------------------------------------------------------------------------------- bind
            final var endpoint = __Utils.parseSocketAddress(_Constants.PORT, args)
                    .orElse(_Constants.SERVER_ENDPOINT_TO_BIND);
            server.bind(endpoint); // IOException
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            // -------------------------------------------------------------------- read '!quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // ------------------------------------------------------------------ keep accepting, receiving, and sending
            while (!server.isClosed()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    assert Thread.currentThread().isDaemon();
                    try {
                        __Utils.logAccepted(client.getRemoteSocketAddress(), client.getLocalSocketAddress());
                        for (int b; !server.isClosed(); ) {
                            b = client.getInputStream().read(); // IOException
                            if (b == -1) {
                                __Utils.logReceivedEof(client.getRemoteSocketAddress());
                                break;
                            }
                            assert b >= 0 && b <= 255;
                            _Utils.logEchoing(b, client.getRemoteSocketAddress());
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
