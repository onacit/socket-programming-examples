package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.util.ArrayList;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp1Server {

    public static void main(final String... args) throws IOException {
        try (var server = new ServerSocket()) {
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            } catch (final Exception e) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, e);
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
            } catch (final Exception e) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, e);
            }
            try {
                server.setReuseAddress(true);
            } catch (final Exception e) {
                log.error("failed to set reuseAddress", e);
            }
            server.bind(_Rfc863Constants.SERVER_ENDPOINT_TO_BIND); // :::20009
            log.info("bound to {}", server.getLocalSocketAddress());
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                final var clients = new ArrayList<Socket>();
                _Rfc863Utils.readQuitAndClose(server);
                while (!server.isClosed()) {
                    final Socket client;
                    try {
                        client = server.accept();
                        log.debug("accepted from {}", client.getRemoteSocketAddress());
                    } catch (final IOException ioe) {
                        if (!server.isClosed()) {
                            log.error("failed to accept", ioe);
                        }
                        continue;
                    }
                    executor.submit(() -> {
                        clients.add(client);
                        try (var c = client) {
                            {
                                c.shutdownOutput(); // ???
                                try {
                                    client.getOutputStream().write(0);
                                    throw new AssertionError("should not reach here");
                                } catch (final IOException ioe) {
                                    // expected
                                }
                            }
                            for (int r; (r = c.getInputStream().read()) != -1; ) {
                                log.debug("discarding {} received from {}", String.format("0x%1$02x", r),
                                          c.getRemoteSocketAddress());
                            }
                        } finally {
                            final var removed = clients.remove(client);
                            assert removed;
                        }
                        return null;
                    });
                }
                clients.forEach(c -> {
                    try {
                        c.close();
                    } catch (final IOException ioe) {
                        throw new RuntimeException("failed to close " + c, ioe);
                    }
                });
            }
        }
    }
}
