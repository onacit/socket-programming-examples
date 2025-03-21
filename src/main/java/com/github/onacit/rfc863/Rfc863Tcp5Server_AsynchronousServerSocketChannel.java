package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
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
class Rfc863Tcp5Server_AsynchronousServerSocketChannel extends Rfc863Tcp$Server {

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
            log.info("bound to {}", server.getLocalAddress());
            // --------------------------------------------------------------------------------------- prepare a <latch>
            final var latch = new CountDownLatch(1);
            // ------------------------------------------------------------------------------ read 'quit', break <latch>
            __Utils.readQuitAndRun(true, latch::countDown);
            // -------------------------------------------------------------------------------------------------- accept
            server.accept(
                    null,                       // <attachment>
                    new CompletionHandler<>() { // <handler> // @formatter:off
                        @Override
                        public void completed(final AsynchronousSocketChannel client, final Object attachment) {
                            final SocketAddress remoteAddress;
                            try {
                                remoteAddress = client.getRemoteAddress(); // IOException
                            } catch (final IOException ioe) {
                                failed(ioe, attachment);
                                return;
                            }
                            log.debug("accepted from {}", remoteAddress);
                            // ------------------------------------------------------------------ try to shutdown output
                            try {
                                client.shutdownOutput(); // IOException
                            } catch (final IOException ioe) {
                                failed(ioe, attachment);
                                return;
                            }
                            // ------------------------------------------------------------------------------------ read
                            final var dst = ByteBuffer.allocate(1);
                            assert dst.capacity() > 0;
                            client.read(
                                    dst,                        // <dst>
                                    remoteAddress,              // <attachment>
                                    new CompletionHandler<>() { // <handler>
                                        @Override
                                        public void completed(final Integer r, final SocketAddress attachment) {
                                            if (r == -1) {
                                                return;
                                            }
                                            for (dst.flip(); dst.hasRemaining();) {
                                                log.debug("discarding {} received from {}",
                                                          String.format("0x%02X", dst.get()),
                                                          remoteAddress
                                                );
                                            }
                                            client.read(dst.clear(), attachment, this);
                                        }
                                        @Override
                                        public void failed(final Throwable exc, final SocketAddress attachment) {
                                            log.error("failed to read", exc);
                                            try {
                                                client.close();
                                            } catch (final IOException ioe) {
                                                log.error("failed to close " + client, ioe);
                                            }
                                        }
                                    }
                            );
                            server.accept(null, this);
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to accept", exc);
                            latch.countDown();
                        } // @formatter:on
                    }
            );
            // ------------------------------------------------------------------------------ await <latch> to be broken
            latch.await(); // InterruptedException
        }
    }
}
