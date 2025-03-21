package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc863Tcp5Client_AsynchronousSocketChannel extends Rfc863Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        final var group = AsynchronousChannelGroup.withFixedThreadPool(1, Executors.defaultThreadFactory());
        try (var client = AsynchronousSocketChannel.open(group)) { // IOException
            // --------------------------------------------------- read 'quit', close the <client>, shutdown the <group>
            __Utils.readQuitAndCall(true, () -> {
                client.close();
                group.shutdown();
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
                                            if (group.isShutdown()) {
                                                return;
                                            }
                                            if (_Constants.THROTTLE) {
                                                try {
                                                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
                                                } catch (final InterruptedException ie) {
                                                    Thread.currentThread().interrupt();
                                                    failed(ie, attachment);
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
                                        }
                                    }
                            );
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to connect", exc);
                            try {
                                client.close();
                            } catch (final IOException ioe) {
                                log.error("failed to close socket", ioe);
                            }
                            group.shutdown();
                        }
                    } // @formatter:on
            );
            // -------------------------------------------------------------------------- await <group> to be terminated
            final var terminated = group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); // InterruptedException
            assert terminated;
        }
    }
}
