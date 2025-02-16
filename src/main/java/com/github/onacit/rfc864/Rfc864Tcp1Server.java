package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp1Server {

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
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalSocketAddress());
            __Utils.readQuitAndClose(server);
            while (!server.isClosed()) {
                final Socket client;
                try {
                    client = server.accept(); // blocking-call
                } catch (final IOException ioe) {
                    if (!server.isClosed()) {
                        log.error("failed to accept", ioe);
                    }
                    continue;
                }
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteSocketAddress());
                        client.shutdownInput(); // IOException
                        if (true) {
                            final var buffer = ByteBuffer.allocate(2).flip();
                            assert buffer.position() == 0;
                            assert buffer.limit() == 0;
                            assert !buffer.hasRemaining();
                            final var generator = _Utils.newPatternGenerator();
                            while (!server.isClosed()) {
                                for (generator.generate(buffer.compact()).flip(); buffer.hasRemaining(); ) {
                                    client.getOutputStream().write(buffer.get());
                                }
                                Thread.sleep(ThreadLocalRandom.current().nextInt(128)); // InterruptedException
                            }
                        }
//                        final var buffer = _Utils.newBuffer();
//                        for (int i = 0, j = 0; !server.isClosed(); ) {
//                            if (!buffer.hasRemaining()) {
//                                buffer.position(0);
//                            }
//                            client.getOutputStream().write(buffer.get()); // IOException
//                            if (++i == 72) {
//                                client.getOutputStream().write('\r'); // CR; IOException
//                                client.getOutputStream().write('\n'); // LF; IOException
//                                i = 0;
//                                buffer.position(++j % buffer.capacity());
//                            }
//                            Thread.sleep(ThreadLocalRandom.current().nextInt(128)); // InterruptedException
//                        }
                    } finally {
                        client.close();
                    }
                    return null;
                });
            }
        }
    }
}
