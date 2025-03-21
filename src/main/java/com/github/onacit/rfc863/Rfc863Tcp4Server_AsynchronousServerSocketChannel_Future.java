package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp4Server_AsynchronousServerSocketChannel_Future extends Rfc863Tcp$Server {

    public static void main(final String... args) throws IOException, InterruptedException, ExecutionException {
        try (var server = AsynchronousServerSocketChannel.open()) { // IOException
            // ------------------------------------------------------------------------------- try to reuse address/port
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            // ---------------------------------------------------------------------------------------------------- bind
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress());
            // --------------------------------------------------------------------- read 'quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // ------------------------------------------------------------------------------------------ keep accepting
            try (var executor = Executors.newCachedThreadPool()) {
                while (server.isOpen()) {
                    final var accepting = server.accept();
                    final var client = accepting.get(); // InterruptedException, ExecutionException
                    executor.submit(() -> {
                        try {
                            final var remoteAddress = client.getRemoteAddress(); // IOException
                            log.debug("accepted from {}, through {}", remoteAddress,
                                      client.getLocalAddress() // IOException
                            );
                            final var dst = ByteBuffer.allocate(1);
                            assert dst.capacity() > 0;
                            while (server.isOpen()) {
                                final var reading = client.read(dst.clear());
                                final var r = reading.get(); // ExecutionException, InterruptedException
                                if (r == -1) {
                                    assert !client.isOpen();
                                    break;
                                }
                                assert r > 0;
                                for (dst.flip(); dst.hasRemaining(); ) {
                                    log.debug("discarding {} received from {}", String.format("0x%02X", dst.get()),
                                              remoteAddress);
                                }
                            }
                        } finally {
                            client.close(); // IOException
                        }
                        return null;
                    });
                }
            }
        }
    }
}
