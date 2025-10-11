package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

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
                            log.debug("accepted from {}, through {}",
                                      client.getRemoteAddress(), // IOException
                                      client.getLocalAddress() // IOException
                            );
                            // -------------------------------------------------------------- shutdown output (optional)
                            if (_Constants.TCP_SERVER_SHUTDOWN_CLIENT_OUTPUT) {
                                log.debug("shutting down output...");
                                client.shutdownOutput(); // IOException
                                final var src = ByteBuffer.allocate(ThreadLocalRandom.current().nextInt(2));
                                try {
                                    client.write(src).get(); // InterruptedException, ExecutionException
                                    assert false;
                                } catch (final ExecutionException ee) {
                                    log.debug("failed to write; expected; as output has been shut down", ee);
                                }
                            }
                            // ---------------------------------------------------------------------------- keep reading
                            final var dst = ByteBuffer.allocate(1);
                            assert dst.capacity() > 0;
                            while (server.isOpen()) {
                                final var reading = client.read(dst.clear());
                                final var r = reading.get(); // ExecutionException, InterruptedException
                                if (r == -1) {
                                    break;
                                }
                                assert r > 0;
                                for (dst.flip(); dst.hasRemaining(); ) {
                                    log.debug("discarding {} received from {}", String.format("0x%02X", dst.get()),
                                              client.getRemoteAddress() // IOException
                                    );
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
