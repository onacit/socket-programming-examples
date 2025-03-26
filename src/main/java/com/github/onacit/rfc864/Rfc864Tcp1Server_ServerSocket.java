package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp1Server_ServerSocket extends Rfc864Tcp$Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newCachedThreadPool();
             var server = new ServerSocket()) { // close() -> IOException
            // ------------------------------------------------------------------------------- try to reuse address/port
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException yhe) {
                // empty
            }
            server.setReuseAddress(true); // SocketException
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // IOException
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            // ------------------------------------------------------------------------- read 'quit', and close <server>
            __Utils.readQuitAndClose(true, server);
            // ------------------------------------------------------------------------------------------ keep accepting
            while (!server.isClosed()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        log.debug("accepted from {}", client.getRemoteSocketAddress());
                        // ------------------------------------------------------------------- shutdown input (optional)
                        client.shutdownInput(); // IOException
                        // -------------------------------------------------------------------------------- keep sending
                        for (final var generator = _Utils.newPatternGenerator(); !server.isClosed(); ) {
                            for (final var buffer = generator.buffer(); buffer.hasRemaining(); ) {
                                client.getOutputStream().write(buffer.get()); // IOException
                            }
                            // sleep just the sanity
                            Thread.sleep(ThreadLocalRandom.current().nextInt(128)); // InterruptedException
                        }
                        client.getOutputStream().flush(); // IOException
                    } finally {
                        client.close(); // IOException
                    }
                    return null;
                });
            }
        }
    }
}
