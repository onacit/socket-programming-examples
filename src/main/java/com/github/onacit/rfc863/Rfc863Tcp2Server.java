package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp2Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = ServerSocketChannel.open()) {
            {
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uoe);
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uoe);
                }
                server.socket().setReuseAddress(true); // SocketException
            }
            {
                assert !server.socket().isBound();
                server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
                log.info("bound to {}", server.getLocalAddress());
                assert server.socket().isBound();
            }
            {
                __Utils.readQuitAndClose(true, server);
            }
            while (server.isOpen()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                        for (final var dst = ByteBuffer.allocate(1);
                             server.isOpen() && client.read(dst.clear()) != -1; ) {
                            log.debug("discarding {} received from {}", String.format("0x%1$02X", dst.get(0)),
                                      client.getRemoteAddress());
                        }
                    } finally {
                        client.close();
                    }
                    return null;
                });
            } // end-of-while-loop
        } // end-of-try-with-resources
    }
}
