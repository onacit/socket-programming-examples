package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc863Tcp5Server_AsynchronousServerSocketChannel extends Rfc863Tcp$Server {

    public static void main(final String... args) throws IOException, InterruptedException {
        final var group = AsynchronousChannelGroup.withThreadPool(Executors.newVirtualThreadPerTaskExecutor());
        try (var server = AsynchronousServerSocketChannel.open(group)) { // IOException
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
            // ------------------------------------------------------ read 'quit', shutdown/await <group>, break <latch>
            __Utils.readQuitAndCall(true, () -> {
                group.shutdownNow();
                final var duration = Duration.ofSeconds(4L);
                final var terminated = group.awaitTermination(duration.getSeconds(), TimeUnit.SECONDS);
                if (!terminated) {
                    log.error("not terminated in {}", duration);
                }
                latch.countDown();
                return null;
            });
            // -------------------------------------------------------------------------------------------------- accept
            server.accept( // @formatter:off
                    null,                       // <attachment>
                    new CompletionHandler<>() { // <handler>
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
                            // ------------------------------------------------------------------------- shutdown output
                            try {
                                client.shutdownOutput(); // IOException
                            } catch (final IOException ioe) {
                                failed(ioe, attachment);
                                return;
                            }
                            // ------------------------------------------------------------------------------------ read
                            final var dst = ByteBuffer.allocate(1);
                            client.read(
                                    dst,                        // <dst>
                                    remoteAddress,              // <attachment>
                                    new CompletionHandler<>() { // <handler>
                                        @Override
                                        public void completed(final Integer r, final SocketAddress attachment) {
                                            if (r == -1) {
                                                assert !client.isOpen();
                                                return;
                                            }
                                            for (dst.flip(); dst.hasRemaining();) {
                                                log.debug("discarding {} received from {}",
                                                          String.format("0x%02X", dst.get()),
                                                          remoteAddress
                                                );
                                            }
//                                            if (group.isShutdown()) {
//                                                try {
//                                                    client.close();
//                                                } catch (final IOException ioe) {
//                                                    log.error("failed to close " + client, ioe);
//                                                }
//                                                return;
//                                            }
                                            // ------------------------------------------------------------ keep reading
                                            client.read(
                                                    dst.clear(), // <dst>
                                                    attachment,  // <attachment>
                                                    this         // <handler>
                                            );
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
                            server.accept(
                                    null, // <attachment>
                                    this  // <handler>
                            );
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to accept", exc);
                        }
                    }
            ); // @formatter:on
            // --------------------------------------------------------------------------await < group > to be terminated
//            final var terminated = group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); // InterruptedException
//            assert terminated;
            // ------------------------------------------------------------------------------------------- await <latch>
            latch.await(); // InterruptedException
        }
    }
}
