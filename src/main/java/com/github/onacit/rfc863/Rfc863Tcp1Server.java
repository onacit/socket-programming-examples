package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
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
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uoe);
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uoe);
                }
                try {
                    server.setReuseAddress(true);
                } catch (final SocketException se) {
                    log.error("failed to set reuseAddress", se);
                }
            }
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // :::20009
            log.info("bound to {}", server.getLocalSocketAddress());
            __Utils.readQuitAndClose(true, server);
            while (!server.isClosed()) {
                final Socket client;
                client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        assert Thread.currentThread().isDaemon(); // virtual thread
                        log.debug("accepted from {}", client.getRemoteSocketAddress());
                        for (int r; (r = client.getInputStream().read()) != -1 && !server.isClosed(); ) { // IOException
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
