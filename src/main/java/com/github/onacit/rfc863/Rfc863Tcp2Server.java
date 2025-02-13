package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp2Server {

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
            _Utils.readQuitAndClose(server);
            while (server.isOpen()) {
                final SocketChannel client;
                try {
                    client = server.accept(); // non-blocking mode -> blocking call
                } catch (final AsynchronousCloseException acc) {
                    assert !server.isOpen();
                    continue;
                }
                log.debug("accepted from {}", client.getRemoteAddress()); // ClosedChannelException, IOException
                executor.submit(() -> {
                    try {
                        final var dst = ByteBuffer.allocate(1);
                        for (int r; (r = client.read(dst.clear())) != -1 && server.isOpen(); ) {
                            assert r == 1;
                            log.debug("discarding {} received from {}", String.format("0x%1$02x", dst.get(0)),
                                      client.getRemoteAddress());
                        } // end-of-for
                    } finally {
                        client.close();
                    }
                    return null;
                });
            }
        } // end-of-try-with-resources
    }
}
