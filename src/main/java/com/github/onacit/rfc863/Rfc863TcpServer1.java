package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863TcpServer1 {

    public static void main(final String... args) throws Exception {
        try (var server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(_Rfc863Constants.ENDPOINT);
            log.info("bound to {}", server.getLocalSocketAddress());
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    final var client = server.accept();
                    executor.submit(() -> {
                        try (var c = client) {
                            log.debug("accepted from {}", c.getRemoteSocketAddress());
                            while (c.getInputStream().read() != -1) {
                                // does nothing
                            }
                        }
                        return null;
                    });
                }
            }
        }
    }
}
