package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * .
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see <a href="https://bit.ly/4kEo35j">java.nio.channels.AsynchronousChannelGroup</a>
 * @see <a href="https://bit.ly/4kKw08Y">java.nio.channels.AsynchronousServerSocket</a>
 * @see <a href="https://bit.ly/4ihblHX">java.nio.channels.AsynchronousSocket</a>
 */
@Slf4j
class Rfc863Tcp5Server_AsynchronousServerSocketChannel extends Rfc862Tcp$Server {

    public static void main(final String... args) throws IOException, InterruptedException {
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
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // IOException
            log.info("bound to {}", server.getLocalAddress()); // IOException
            // --------------------------------------------------------------------------------------- prepare a <latch>
            final var latch = new CountDownLatch(1);
            // -------------------------------------------------------------------------- read 'quit', break the <latch>
            __Utils.readQuitAndCountDown(true, latch);
            // -------------------------------------------------------------------------------------------------- accept
            server.accept( // @formatter:off
                    null,                       // <attachment>
                    new CompletionHandler<>() { // <handler>
                        @Override
                        public void completed(final AsynchronousSocketChannel client, final Object attachment) {
                            try {
                                log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                            } catch (final IOException ioe) {
                                log.error("failed to get remote address of {}", client, ioe);
                            }
                            // -------------------------------------------------------------- shutdown output (optional)
                            if (_Constants.TCP_SERVER_SHUTDOWN_OUTPUT) {
                                try {
                                    client.shutdownOutput(); // IOException
                                    client.write(ByteBuffer.allocate(1), null, new CompletionHandler<>() {
                                        @Override public void completed(final Integer result, final Object attachment) {
                                            log.debug("completed");
                                            assert false: "aaa";
                                        }
                                        @Override public void failed(Throwable exc, Object attachment) {
                                            log.debug("failed to write; expected; output has been shut down", exc);
                                        }
                                    });
                                } catch (final IOException ioe) {
                                    log.error("failed to shutdown output of {}", client, ioe);
                                }
                            }
                            // ------------------------------------------------------------------------------------ read
                            final var dst = ByteBuffer.allocate(1); // may increase the capacity
                            assert dst.capacity() > 0;
                            client.read(
                                    dst,                        // <dst>
                                    null,                       // <attachment>
                                    new CompletionHandler<>() { // <handler>
                                        @Override public void completed(final Integer r, final Object attachment) {
                                            if (r == -1) {
                                                try {
                                                    client.close();
                                                } catch (final IOException ioe) {
                                                    log.error("failed to close {}", client, ioe);
                                                }
                                                return;
                                            }
                                            for (dst.flip(); dst.hasRemaining();) {
                                                try {
                                                    log.debug("discarding {} received from {}",
                                                              String.format("0x%02X", dst.get()),
                                                              client.getRemoteAddress() // IOException
                                                    );
                                                } catch (final IOException ioe) {
                                                    log.error("failed to get remote address of {}", client, ioe);
                                                }
                                            }
                                            client.read(
                                                    dst.clear(), // <dst>
                                                    attachment,  // <attachment>
                                                    this         // <handler>
                                            );
                                        }
                                        @Override public void failed(final Throwable exc, final Object attachment) {
                                            log.error("failed to read; attachment: {}", attachment, exc);
                                            try {
                                                client.close();
                                            } catch (final IOException ioe) {
                                                log.error("failed to close {}", client, ioe);
                                            }
                                        }
                                    }
                            );
                            server.accept(null, this);
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to accept; attachment: {}", attachment, exc);
                            latch.countDown();
                        }
                    } // @formatter:on
            );
            // ------------------------------------------------------------------------------ await <latch> to be broken
            latch.await(); // InterruptedException
        }
    }
}
