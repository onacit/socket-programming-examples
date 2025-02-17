package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp4Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = AsynchronousSocketChannel.open()) {
            {
                try {
                    client.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
                } catch (final Exception e) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, e);
                }
                try {
                    client.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
                } catch (final Exception e) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, e);
                }
            }
            final var latch = new CountDownLatch(1);
            __Utils.readQuitAndRun(false, latch::countDown); // @formatter:off
            client.connect(_Constants.SERVER_ENDPOINT, null, new CompletionHandler<>() {
                @Override public void completed(final Void result, final Object attachment) {
                    try {
                        log.debug("connected to {}, through {}", client.getRemoteAddress(),
                                  client.getLocalAddress());
                    } catch (final IOException ioe) {
                        throw new RuntimeException("failed to get addresses of " + client, ioe);
                    }
                    final var dst = ByteBuffer.allocate(1);
                    ThreadLocalRandom.current().nextBytes(dst.array());
                    client.read(dst, null, new CompletionHandler<>() {
                        @Override public void completed(final Integer r, final Object attachment) {
                            assert r == 1; // why?
                            System.out.print((char) dst.flip().get());
                            client.read(dst.clear(), null, this);
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to read", exc);
                            latch.countDown();
                        }
                    });
                }
                @Override public void failed(final Throwable exc, final Object attachment) {
                    log.error("failed to connect", exc);
                    latch.countDown();
                }
            }); // @formatter:on
            latch.await(); // InterruptedException
        }
    }
}
