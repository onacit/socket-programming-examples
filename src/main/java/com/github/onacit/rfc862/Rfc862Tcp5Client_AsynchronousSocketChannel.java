package com.github.onacit.rfc862;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * .
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see <a href="https://bit.ly/4kEo35j">java.nio.channels.AsynchronousChannelGroup</a>
 * @see <a href="https://bit.ly/4kKw08Y">java.nio.channels.AsynchronousServerSocket</a>
 * @see <a href="https://bit.ly/4ihblHX">java.nio.channels.AsynchronousSocket</a>
 */
@Slf4j
class Rfc862Tcp5Client_AsynchronousSocketChannel extends Rfc862Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        final var group = AsynchronousChannelGroup.withThreadPool(Executors.newVirtualThreadPerTaskExecutor());
        try (var client = AsynchronousSocketChannel.open(group)) { // IOException
            // --------------------------------------------------------------------------------- connect, asynchronously
            final var remote = __Utils.parseSocketAddress(_Constants.PORT, args).orElse(_Constants.SERVER_ENDPOINT);
            client.connect(remote, null, new CompletionHandler<>() {
                @Override
                public void completed(final Void result, final Object attachment) {
                    try {
                        log.debug("connected to {}, through {}",
                                  client.getRemoteAddress(), // IOException
                                  client.getLocalAddress() // IOException
                        );
                    } catch (final IOException ioe) {
                        log.error("failed to get address from {}", client, ioe);
                    }
                    __Utils.readQuitAndShutdownNow(true, group, l -> {
                        final var buffer = ByteBuffer.wrap(
                                (l + System.lineSeparator()).getBytes(__Constants.CHARSET)
                        );
                        final var latch = new CountDownLatch(1);
                        client.write(buffer, null, new CompletionHandler<>() {
                            @Override
                            public void completed(final Integer result,
                                                  final Object attachment) {
                                if (buffer.hasRemaining()) {
                                    client.write(buffer, null, this);
                                    return;
                                }
                                buffer.clear();
                                client.read(buffer, null, new CompletionHandler<>() {
                                    @Override
                                    public void completed(final Integer result,
                                                          final Object attachment) {
                                        if (result == -1) {
                                            latch.countDown();
                                            return;
                                        }
                                        if (buffer.hasRemaining()) {
                                            client.read(buffer, null, this);
                                            return;
                                        }
                                        System.out.print(__Constants.CHARSET.decode(
                                                buffer.flip()));
                                        buffer.clear();
                                        latch.countDown();
                                    }

                                    @Override
                                    public void failed(final Throwable exc,
                                                       final Object attachment) {
                                        log.error("failed to read", exc);
                                        latch.countDown();
                                    }
                                });
                            }

                            @Override
                            public void failed(final Throwable exc,
                                               final Object attachment) {
                                log.error("failed to write", exc);
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (final InterruptedException ie) {
                            log.error("interrupted while awaiting latch", ie);
                            Thread.currentThread().interrupt();
                        }
                    });
                }

                @Override
                public void failed(final Throwable exc, final Object attachment) {
                    log.error("failed to connect", exc);
                    try {
                        group.shutdownNow();
                    } catch (final IOException ioe) {
                        log.error("failed to shutdown {}", group, ioe);
                    }
                } // @formatter:on
            });
            // --------------------------------------------------------------------------------------- await the <latch>
            group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }
    }
}
