package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863TcpServer1 {

    public static void main(final String... args) throws Exception {
        try (var server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(_Rfc863Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalSocketAddress());
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    final var client = server.accept();
                    executor.submit(() -> {
                        try (var c = client) {
                            log.debug("accepted from {}", c.getRemoteSocketAddress());
                            {
                                c.shutdownOutput(); // ???
                                try {
                                    client.getOutputStream().write(0);
                                    throw new AssertionError("should not reach here");
                                } catch (final IOException ioe) {
                                    // expected
                                }
                            }
//                            while (c.getInputStream().read() != -1) {
//                                // does nothing
//                            }
                            for (int r; (r = c.getInputStream().read()) != -1; ) {
                                log.debug("discarding {} received from {}", String.format("%1$02x", r),
                                        c.getRemoteSocketAddress());
                            }
                        }
                        return null;
                    });
                }
            }
        }
    }
}
