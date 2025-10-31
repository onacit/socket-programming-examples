package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
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
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (_Constants.TCP_CLIENT_BIND) {
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0));
                log.debug("bound to {}", client.getLocalAddress());
            }
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
                            // --------------------------------------------------------------- shutdown input (optional)
                            if (_Constants.TCP_CLIENT_SHUTDOWN_INPUT) {
                                try {
                                    client.shutdownInput(); // IOException
                                    client.read(ByteBuffer.allocate(1), null, new CompletionHandler<>() {
                                        @Override public void completed(final Integer result, Object attachment) {
                                            assert result == -1;
                                        }
                                        @Override public void failed(final Throwable exc, Object attachment) {
                                            assert false;
                                        }});
                                } catch (final IOException ioe) {
                                    log.error("failed to shutdown input", ioe);
                                }
                            }
                            // --------------------------------------------------------------- keep sending random bytes
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
                                                    Thread.currentThread().interrupt();
                                                    throw new RuntimeException("interrupted while sleeping", ie);
                                                }
                                            }
                                            client.write(
                                                    __Utils.randomizeAvailableAndContent(src), // <src>
                                                    attachment,                                // <attachment>
                                                    this                                       // <handler>
                                            );
                                        }
                                        @Override public void failed(final Throwable exc, final Object attachment) {
                                            log.error("failed to write; attachment: {}", attachment, exc);
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
