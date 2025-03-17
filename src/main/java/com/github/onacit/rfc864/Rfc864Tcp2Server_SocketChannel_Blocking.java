package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp2Server_SocketChannel_Blocking {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = ServerSocketChannel.open()) {
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
                    server.socket().setReuseAddress(true);
                } catch (final Exception e) {
                    log.error("failed to set reuseAddress", e);
                }
            }
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress());
            __Utils.readQuitAndClose(true, server);
            assert server.isBlocking(); // !!!
            while (server.isOpen()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                        client.shutdownInput(); // IOException
                        for (final var generator = _Utils.newPatternGenerator(); server.isOpen(); ) {
                            final var w = client.write(generator.buffer());
                            Thread.sleep(ThreadLocalRandom.current().nextInt(128)); // InterruptedException
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
