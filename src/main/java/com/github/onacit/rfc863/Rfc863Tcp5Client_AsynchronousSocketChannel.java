package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc863Tcp5Client_AsynchronousSocketChannel extends Rfc863Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        final var group = AsynchronousChannelGroup.withFixedThreadPool(1, Executors.defaultThreadFactory());
        try (var client = AsynchronousSocketChannel.open(group)) { // IOException
            // ----------------------------------------------------------------------------------------- prepare a latch
            final var latch = new CountDownLatch(1);
            // --------------------------------------------------- read 'quit', close the <client>, shutdown the <group>
            __Utils.readQuitAndCall(true, () -> {
                try {
                    group.shutdownNow(); // IOException
                    final var duration = Duration.ofSeconds(4L);
                    final var terminated = group.awaitTermination(duration.getSeconds(), TimeUnit.SECONDS);
                    if (!terminated) {
                        log.error("not terminated in {}", duration);
                    }
                } finally {
                    latch.countDown();
                }
                return null;
            });
            // --------------------------------------------------------------------------------- connect, asynchronously
            client.connect( // @formatter:off
                    _Constants.SERVER_ENDPOINT, // <remote>
                    null,                       // <attachment>
                    new CompletionHandler<>() { // <handler>
                        @Override public void completed(final Void result, final Object attachment) {
                            try {
                                log.debug("connected to {}, through {}",
                                          client.getRemoteAddress(), // IOException
                                          client.getLocalAddress() // IOException
                                );
                            } catch (final IOException ioe) {
                                failed(ioe, attachment);
                                return;
                            }
                            final var src = ByteBuffer.allocate(1);
                            assert src.capacity() > 0;
                            client.write(
                                    __Utils.randomize(src),     // <src>
                                    null,                       // <attachment>
                                    new CompletionHandler<>() { // <handler>
                                        @Override
                                        public void completed(final Integer result, final Object attachment) {
                                            if (_Constants.THROTTLE) {
                                                try {
                                                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
                                                } catch (final InterruptedException ie) {
                                                    log.error("interrupted while sleeping", ie);
                                                    Thread.currentThread().interrupt();
                                                    try {
                                                        client.close();
                                                    } catch (final IOException ioe) {
                                                        log.error("failed to close socket", ioe);
                                                    }
                                                    group.shutdown();
                                                    latch.countDown();
                                                    return;
                                                }
                                            }
                                            client.write(
                                                    __Utils.randomize(src.clear()), // <src>
                                                    null,                           // <attachment>
                                                    this                            // <handler>
                                            );
                                        }
                                        @Override public void failed(final Throwable exc, final Object attachment) {
                                            log.error("failed to write", exc);
                                            try {
                                                client.close();
                                            } catch (final IOException ioe) {
                                                log.error("failed to close socket", ioe);
                                            }
                                            group.shutdown();
                                            latch.countDown();
                                        }
                                    }
                            );
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to connect", exc);
                            group.shutdown();
                            latch.countDown();
                        }
                    } // @formatter:on
            );
            // -------------------------------------------------------------------------- await <group> to be terminated
//            final var terminated = group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); // InterruptedException
//            assert terminated;
            latch.await();
        }
    }
}
