package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp2Server_ServerSocketChannel_Blocking {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = ServerSocketChannel.open()) { // IOException
            {
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uhe) {
                    // empty
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uhe) {
                    // empty
                }
                server.socket().setReuseAddress(true); // SocketException
            }
            {
                server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
                log.info("bound to {}", server.getLocalAddress());
            }
            {
                __Utils.readQuitAndClose(true, server);
            }
            {
                assert server.isBlocking(); // !!!
            }
            while (server.isOpen()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                        client.shutdownInput(); // IOException
                        for (final var generator = _Utils.newPatternGenerator(); server.isOpen(); ) {
                            final var w = client.write(generator.buffer());
                            // server doesn't need to sleep, at all
                            Thread.sleep(ThreadLocalRandom.current().nextInt(128)); // InterruptedException
                        }
                    } finally {
                        client.close(); // IOException
                    }
                    return null;
                });
            }
        }
    }
}
