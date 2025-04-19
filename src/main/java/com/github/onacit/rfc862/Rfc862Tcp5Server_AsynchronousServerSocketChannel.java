package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
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
class Rfc862Tcp5Server_AsynchronousServerSocketChannel extends Rfc862Tcp$Server {

    public static void main(final String... args) throws IOException, InterruptedException {
        final var group = AsynchronousChannelGroup.withThreadPool(Executors.newVirtualThreadPerTaskExecutor());
        try (var server = AsynchronousServerSocketChannel.open()) { // IOException
            // ------------------------------------------------------------------------------- try to reuse address/port
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            // ---------------------------------------------------------------------------------------------------- bind
            server.bind(
                    __Utils.parseSocketAddress(_Constants.PORT, args)
                            .orElse(_Constants.SERVER_ENDPOINT_TO_BIND)
            ); // IOException
            log.info("bound to {}", server.getLocalAddress()); // IOException
            // -------------------------------------------------------------------------- read 'quit', break the <latch>
            __Utils.readQuitAndShutdownNow(true, group, l -> {
            });
            // -------------------------------------------------------------------------------------------------- accept
            server.accept(null, new CompletionHandler<>() {
                @Override
                public void completed(final AsynchronousSocketChannel client, final Object attachment) {
                    try {
                        log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                    } catch (final IOException ioe) {
                        log.error("failed to get remote address of {}", client, ioe);
                    }
                    // -------------------------------------------------------------------------------------------- read
                    final var buffer = ByteBuffer.allocate(1); // may increase the capacity
                    assert buffer.capacity() > 0;
                    client.read(buffer, null, new CompletionHandler<>() {
                        @Override
                        public void completed(final Integer r, final Object attachment) {
                            if (r == -1) {
                                try {
                                    client.close();
                                } catch (final IOException ioe) {
                                    log.error("failed to close {}", client, ioe);
                                }
                                return;
                            }
                            final var latch = new CountDownLatch(1);
                            buffer.flip();
                            client.write(buffer, null, new CompletionHandler<>() {
                                @Override
                                public void completed(final Integer result, final Object attachment) {
                                    if (buffer.hasRemaining()) {
                                        client.write(buffer, null, this);
                                        return;
                                    }
                                    buffer.clear();
                                    latch.countDown();
                                }

                                @Override
                                public void failed(final Throwable exc, final Object attachment) {
                                    log.error("failed to write", exc);
                                    latch.countDown();
                                }
                            });
                            try {
                                latch.await();
                            } catch (final InterruptedException ie) {
                                failed(ie, attachment);
                                return;
                            }
                            client.read(buffer, null, this);
                        }

                        @Override
                        public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to read; attachment: {}", attachment, exc);
                        }
                    });
                    server.accept(null, this);
                }

                @Override
                public void failed(final Throwable exc, final Object attachment) {
                    log.error("failed to accept; attachment: {}", attachment, exc);
                }
            });
            // ---------------------------------------------------------------------------------------------------------
            final boolean terminated = group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            assert terminated;
        }
    }
}
