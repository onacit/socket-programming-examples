package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp2Server_ServerSocketChannel_Blocking extends _Rfc863Tcp_Server {

    public static void main(final String... args) throws IOException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var server = ServerSocketChannel.open()) {
            {
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uoe);
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uoe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uoe);
                }
                server.socket().setReuseAddress(true); // SocketException
            }
            {
                assert !server.socket().isBound();
                if (ThreadLocalRandom.current().nextBoolean()) {
                    server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
                } else {
                    server.socket().bind(_Constants.SERVER_ENDPOINT_TO_BIND);
                }
                log.info("bound to {}", server.getLocalAddress());
                assert server.socket().isBound();
            }
            {
                __Utils.readQuitAndClose(true, server);
            }
            {
                assert server.isBlocking();
            }
            while (server.isOpen()) {
                final SocketChannel client;
                if (ThreadLocalRandom.current().nextBoolean()) {
                    client = server.accept(); // IOException
                } else {
                    client = server.socket().accept().getChannel(); // IOException
                }
                executor.submit(() -> {
                    try {
                        final SocketAddress remoteAddress;
                        if (ThreadLocalRandom.current().nextBoolean()) {
                            remoteAddress = client.getRemoteAddress(); // IOException
                        } else {
                            remoteAddress = client.socket().getRemoteSocketAddress();
                        }
                        log.debug("accepted from {}", remoteAddress);
                        for (final var dst = ByteBuffer.allocate(1);
                             server.isOpen() && client.read(dst.clear()) != -1; ) { // IOException
                            final int b;
                            if (ThreadLocalRandom.current().nextBoolean()) {
                                final var r = client.read(dst.clear()); // IOException
                                if (r == -1) {
                                    break;
                                }
                                b = dst.get(0) & 0xFF;
                            } else {
                                b = client.socket().getInputStream().read(); // IOException
                                if (b == -1) {
                                    break;
                                }
                            }
                            log.debug("discarding {} received from {}", String.format("0x%1$02X", b),
                                      remoteAddress); // IOException
                        }
                    } finally {
                        if (ThreadLocalRandom.current().nextBoolean()) {
                            client.close(); // IOException
                        } else {
                            client.socket().close(); // IOException
                        }
                        assert !client.isOpen();
                        assert client.socket().isClosed();
                    }
                    return null;
                });
            } // end-of-while-loop
        } // end-of-try-with-executor/server
    }
}
