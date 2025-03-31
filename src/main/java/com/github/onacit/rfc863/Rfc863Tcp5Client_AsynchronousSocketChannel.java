package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * .
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see <a href="https://bit.ly/4kEo35j">java.nio.channels.AsynchronousChannelGroup</a>
 * @see <a href="https://bit.ly/4kKw08Y">java.nio.channels.AsynchronousServerSocket</a>
 * @see <a href="https://bit.ly/4ihblHX">java.nio.channels.AsynchronousSocket</a>
 */
@Slf4j
class Rfc863Tcp5Client_AsynchronousSocketChannel extends Rfc863Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = AsynchronousSocketChannel.open()) { // IOException
            // ----------------------------------------------------------------------------------------- prepare a latch
            final var latch = new CountDownLatch(1);
            // -------------------------------------------------------------------------- read 'quit', break the <latch>
            __Utils.readQuitAndCountDown(true, latch);
            // --------------------------------------------------------------------------------- connect, asynchronously
            client.connect(
                    _Constants.SERVER_ENDPOINT, // <remote>
                    null,                       // <attachment>
                    new CompletionHandler<>() { // <handler> // @formatter:off
                        @Override public void completed(final Void result, final Object attachment) {
                            try {
                                log.debug("connected to {}, through {}",
                                          client.getRemoteAddress(), // IOException
                                          client.getLocalAddress() // IOException
                                );
                            } catch (final IOException ioe) {
                                log.error("failed to get address from {}", client, ioe);
                            }
                            final var src = ByteBuffer.allocate(1);
                            assert src.capacity() > 0;
                            client.write(
                                    __Utils.randomizeAvailableAndContent(src), // <src>
                                    null,                                      // <attachment>
                                    new CompletionHandler<>() {                // <handler>
                                        @Override public void completed(final Integer result, final Object attachment) {
                                            if (_Constants.THROTTLE) {
                                                try {
                                                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
                                                } catch (final InterruptedException ie) {
                                                    throw new RuntimeException("interrupted while sleeping", ie);
                                                }
                                            }
                                            client.write(__Utils.randomizeAvailableAndContent(src), null, this);
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
                        } // @formatter:on
                    }
            );
            // --------------------------------------------------------------------------------------- await the <latch>
            latch.await();
        }
    }
}
