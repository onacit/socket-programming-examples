package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp4Server extends _Rfc863Tcp_Server {

    public static void main(final String... args) throws IOException, InterruptedException, ExecutionException {
        try (var server = AsynchronousServerSocketChannel.open()) { // IOException

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
            }

            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress());

            __Utils.readQuitAndClose(true, server);


            try (var executor = Executors.newCachedThreadPool()) {
                while (server.isOpen()) {
                    final var accepting = server.accept();
                    final var client = accepting.get(); // InterruptedException, ExecutionException
                    final var submitted = executor.submit(() -> {
                        try {
                            log.debug("accepted from {}, through {}",
                                      client.getRemoteAddress(), // IOException
                                      client.getLocalAddress() // IOException
                            );
                            for (final var dst = ByteBuffer.allocate(1); server.isOpen(); ) {
                                final var reading = client.read(dst.clear());
                                final var r = reading.get();
                                if (r == -1) {
                                    break;
                                }
                                assert r > 0;
                                log.debug("discarding 0x{} received from {}",
                                          String.format("%1$02X", dst.flip().get(0)),
                                          client.getRemoteAddress() // IOException
                                );
                            }
                        } finally {
                            client.close();
                        }
                        return null;
                    });
                } // end-of-while-loop
            } // end-of-try-with-executor
        } // end-of-try-with-server
    }
}
