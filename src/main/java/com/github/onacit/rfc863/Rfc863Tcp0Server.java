package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;

/**
 * A minimal TCP server that discards bytes received from a client.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Rfc863Tcp0Server {

    private static final InetAddress ADDR = InetAddress.getLoopbackAddress();

    private static final int PORT = 10000 + __RFC863_Constants.PORT;

    static final SocketAddress ENDPOINT = new InetSocketAddress(ADDR, PORT);

    public static void main(final String... args) throws IOException {
        try (var server = new ServerSocket()) {
            {
                server.bind(ENDPOINT); // IOException
                log.info("bound to {}", server.getLocalSocketAddress());
            }
            try (var client = server.accept()) { // IOException
                log.debug("accepted from {}", client.getRemoteSocketAddress());
                for (int b; (b = client.getInputStream().read()) != -1; ) { // IOException
                    log.debug("discarding {} received from {}", String.format("0x%1$02X", b),
                              client.getRemoteSocketAddress());
                }
            }
        }
    }
}
