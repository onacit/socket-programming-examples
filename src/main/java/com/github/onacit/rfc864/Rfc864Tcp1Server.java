package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp1Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newCachedThreadPool();
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
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalSocketAddress());
            __Utils.readQuitAndClose(true, server);
            while (!server.isClosed()) {
                final Socket client;
                try {
                    client = server.accept(); // IOException
                } catch (final IOException ioe) {
                    server.close();
                    throw ioe;
                }
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteSocketAddress());
                        client.shutdownInput(); // IOException
                        final var generator = _Utils.newPatternGenerator();
                        while (!server.isClosed()) {
                            for (final var b = generator.buffer(); b.hasRemaining(); ) {
                                client.getOutputStream().write(b.get());
                            }
                            Thread.sleep(ThreadLocalRandom.current().nextInt(128)); // InterruptedException
                        }
                    } finally {
                        client.close();
                    }
                    return null;
                });
            } // end-of-accept-submit-loop
        } // end-of-try-with-resources
    }
}
