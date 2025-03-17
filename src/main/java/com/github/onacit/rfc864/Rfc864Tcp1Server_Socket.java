package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp1Server_Socket extends _Rfc864Tcp_Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newCachedThreadPool();
             var server = new ServerSocket()) {
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
            } catch (final UnsupportedOperationException yhe) {
                // empty
            }
            server.setReuseAddress(true); // SocketException
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalSocketAddress());
            __Utils.readQuitAndClose(true, server);
            while (!server.isClosed()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteSocketAddress());
                        client.shutdownInput(); // IOException
                        for (final var generator = _Utils.newPatternGenerator(); !server.isClosed(); ) {
                            for (final var buffer = generator.buffer(); buffer.hasRemaining(); ) {
                                client.getOutputStream().write(buffer.get()); // IOException
                            }
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
