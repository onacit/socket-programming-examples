package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;

@Slf4j
class Rfc863Tcp2Server_ServerSocketChannel_Blocking extends Rfc863Tcp$Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = ServerSocketChannel.open()) { // IOException
            // ------------------------------------------------------------------------------- try to reuse address/port
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uoe) {
                // empty
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uoe) {
                // empty
            }
            server.socket().setReuseAddress(true); // SocketException
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.socket().isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            assert server.socket().isBound();
            log.info("bound to {}", server.getLocalAddress());
            // --------------------------------------------------------------------- read 'quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // ---------------------------------------------------------------------------------------------------------
            assert server.isBlocking(); // !!!
            while (server.isOpen()) {
                final var client = server.accept(); // IOException
                executor.submit(() -> {
                    try {
                        final var remoteAddress = client.getRemoteAddress(); // IOException
                        log.debug("accepted from {}", remoteAddress);
                        for (final var dst = ByteBuffer.allocate(1); server.isOpen(); dst.clear()) {
                            final var r = client.read(dst); // IOException
                            if (r == -1) {
                                break;
                            }
                            assert dst.capacity() == 0 || r > 0;
                            for (dst.flip(); dst.hasRemaining(); ) {
                                log.debug("discarding {} received from {}", String.format("0x%1$02X", dst.get()),
                                          remoteAddress);
                            }
                        }
                    } finally {
                        client.close(); // IOException
                        assert !client.isOpen();
                        assert client.socket().isClosed();
                    }
                    return null;
                });
            }
        }
    }
}
