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

@Slf4j
class Rfc863Tcp5Server_AsynchronousServerSocketChannel extends Rfc863Tcp$Server {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var server = AsynchronousServerSocketChannel.open()) {
            assert server.isOpen();
            // -------------------------------------------------------------------------------------- reuse address/port
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
            // ---------------------------------------------------------------------------------------------------------
            final var latch = new CountDownLatch(1);
            // ----------------------------------------------------------------- read 'quit', and count down the <latch>
            __Utils.readQuitAndRun(false, latch::countDown);
            // -------------------------------------------------------------------------------------------------- accept
            server.accept( // @formatter:off
                    null,                       // <attachment>
                    new CompletionHandler<>() { // <handler>
                        @Override
                        public void completed(final AsynchronousSocketChannel client, final Object attachment) {
                            final SocketAddress remoteAddress;
                            {
                                SocketAddress rs;
                                try {
                                    rs = client.getRemoteAddress();
                                    log.debug("accepted from {}", client.getRemoteAddress());
                                } catch (final IOException ioe) {
                                    log.error("failed to get remote address of {}", client);
                                    rs = null;
                                }
                                remoteAddress = rs;
                            }
                            // ------------------------------------------------------------------------- shutdown output
                            try {
                                client.shutdownOutput();
                            } catch (final IOException ioe) {
                                throw new RuntimeException("failed to shutdown output", ioe);
                            }
                            // ------------------------------------------------------------------------------------ read
                            final var dst = ByteBuffer.allocate(1);
                            client.read(
                                    dst,                        // <dst>
                                    null,                       // <attachment>
                                    new CompletionHandler<>() { // <handler>
                                        @Override public void completed(final Integer r, final Object attachment) {
                                            if (r == -1) {
                                                try {
                                                    client.close();
                                                } catch (final IOException ioe) {
                                                    throw new RuntimeException("failed to close " + client, ioe);
                                                }
                                                return;
                                            }
                                            assert r == 1; // why?
                                            log.debug("discarding {} received from {}",
                                                      String.format("0x%1$02X", dst.get(0)),
                                                      remoteAddress);
                                            // ------------------------------------------------------------ keep reading
                                            client.read(
                                                    dst.clear(), // <dst>
                                                    null,        // <attachment>
                                                    this         // <handler>
                                            );
                                        }
                                        @Override public void failed(final Throwable exc, final Object attachment) {
                                            log.error("failed to read", exc);
                                            try {
                                                client.close();
                                            } catch (final IOException ioe) {
                                                throw new RuntimeException("failed to close " + client, ioe);
                                            }
                                        }
                                    }
                            );
                            server.accept(null, this);
                        }
                        @Override public void failed(final Throwable exc, final Object attachment) {
                            log.error("failed to accept", exc);
                        }
                    }
            ); // @formatter:on

            latch.await(); // InterruptedException
        }
    }
}
