package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp3Client {

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
            _Rfc863Utils.readQuitAndRun(latch::countDown);
            client.connect( // @formatter:off
                    _Rfc863Constants.SERVER_ENDPOINT,
                    null,
                    new CompletionHandler<>() {
                        @Override
                        public void completed(final Void result, final Object attachment) {
                            try {
                                log.debug("connected to {}", client.getRemoteAddress());
                            } catch (final IOException ioe) {
                                throw new RuntimeException("failed to get remote address of " + client, ioe);
                            }
                            final var src = ByteBuffer.allocate(1);
                            ThreadLocalRandom.current().nextBytes(src.array());
                            client.write(
                                    src,                        // <src>
                                    null,                       // <attachment>
                                    new CompletionHandler<>() { // <handler>
                                        @Override
                                        public void completed(final Integer w, final Object attachment) {
                                            assert w == 1;
                                            try {
                                                Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
                                            } catch (final InterruptedException ie) {
                                                Thread.currentThread().interrupt();
                                            }
                                            ThreadLocalRandom.current().nextBytes(src.array());
                                            client.write(
                                                    src.clear(), // <src>
                                                    null,        // <attachment>
                                                    this         // <handler>
                                            );
                                        }
                                        @Override public void failed(final Throwable exc, final Object attachment) {
                                            log.error("failed to write", exc);
                                        }
                                    }
                            );
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to connect", exc);
                        }
                    }
            ); // @formatter:on
            latch.await();
        }
    }
}
