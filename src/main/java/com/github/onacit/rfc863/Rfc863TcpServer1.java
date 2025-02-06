package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863TcpServer1 {

    public static void main(final String... args) throws Exception {
        try (var server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(InetAddress.getLocalHost(), _Rfc863Constants.PORT));
            log.info("bound to {}", server.getLocalSocketAddress());
            final var thread = Thread.ofPlatform().name("server").start(() -> {
                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    while (!server.isClosed()) {
                        final var client = server.accept();
                        executor.submit(() -> {
                            try (var c = client) {
                                log.debug("accepted from {}", c.getRemoteSocketAddress());
                                c.shutdownOutput();
                                while (c.getInputStream().read() != -1) {
                                    // does nothing
                                }
                            }
                            return null;
                        });
                    }
                } catch (final Exception e) {
                    if (!server.isClosed()) {
                        log.error("failed to run", e);
                    }
                }
            });
            _Rfc863Utils.readQuitAndClose(server);
        }
    }
}
