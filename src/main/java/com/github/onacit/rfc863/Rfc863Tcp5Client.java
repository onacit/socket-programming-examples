package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp5Client extends _Rfc863Tcp_Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = AsynchronousSocketChannel.open()) {

            final var latch = new CountDownLatch(1);

            __Utils.readQuitAndRun(true, latch::countDown);

            client.connect( // @formatter:off
                    _Constants.SERVER_ENDPOINT, // <remote>
                    null,                       // <attachment>
                    new CompletionHandler<>() { // <handler>
                        @Override public void completed(final Void result, final Object attachment) {
                            try {
                                log.debug("connected to {}, through {}", client.getRemoteAddress(),
                                          client.getLocalAddress());
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
                                                return;
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
                                            latch.countDown();
                                        }
                                    }
                            );
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to connect", exc);
                            latch.countDown();
                        }
                    }
            ); // @formatter:on

            latch.await(); // InterruptedException
        }
    }
}
