package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;

/**
 * A minimal TCP server that discards bytes received from a client.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Rfc863Tcp0Server {

    public static void main(final String... args) throws IOException {
        try (var server = new ServerSocket()) {
            assert !server.isBound();
            {
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
                server.setReuseAddress(true); // SocketException
            }
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // IOException
            log.info("bound to {}", server.getLocalSocketAddress());
            assert server.isBound();
            __Utils.readQuitAndClose(true, server);
            try (var client = server.accept()) { // IOException
                log.debug("accepted from {}", client.getRemoteSocketAddress());
                for (int r; (r = client.getInputStream().read()) != -1; ) { // IOException
                    log.debug("discarding {} received from {}", String.format("0x%1$02X", r),
                              client.getRemoteSocketAddress());
                }
            } // end-of-try-with-client
        } // end-of-try-with-server
    }
}
