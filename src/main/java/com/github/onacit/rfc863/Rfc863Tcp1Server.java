package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp1Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = new ServerSocket()) {
            {
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
                } catch (final SocketException se) {
                    log.error("failed to set reuseAddress", se);
                }
            }
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // :::20009
            log.info("bound to {}", server.getLocalSocketAddress());
            _Utils.readQuitAndClose(server);
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
                    try {
                        for (int r; (r = client.getInputStream().read()) != -1 && !server.isClosed(); ) {
                            log.debug("discarding {} received from {}", String.format("0x%1$02x", r),
                                      client.getRemoteSocketAddress());
                        }
                    } finally {
                        client.close();
                    }
                    return null;
                });
            }
        }
    }
}
