package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;

@Slf4j
class Rfc863Tcp2Server {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open();
             var server = ServerSocketChannel.open()) {
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            } catch (final Exception e) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, e);
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
            } catch (final Exception e) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, e);
            }
            try {
                server.socket().setReuseAddress(true);
            } catch (final Exception e) {
                log.error("failed to set reuseAddress", e);
            }
            server.configureBlocking(false);
            server.bind(_Rfc863Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress());
            final var serverKey = server.register(selector, SelectionKey.OP_ACCEPT);
            _Rfc863Utils.readQuitAndCall(() -> {
                serverKey.cancel();
                selector.wakeup();
                return null;
            });
            final var buffer = ByteBuffer.allocate(1);
            while (serverKey.isValid()) {
                selector.select(0L);
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    final var channel = key.channel();
                    if (key.isAcceptable()) {
                        assert channel == server;
                        final var client = ((ServerSocketChannel) channel).accept();
                        log.debug("accepted from {}", client.getRemoteAddress());
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        final var r = ((ReadableByteChannel) channel).read(buffer.clear());
                        if (r == -1) {
                            key.cancel();
                        } else {
                            assert r == 1; // why?
                            log.debug("discarding {} received from {}", String.format("0x%1$02x", buffer.get(0)),
                                      ((SocketChannel) channel).getRemoteAddress());
                        }
                    }
                }
            }
            selector.keys().forEach(k -> {
                k.cancel(); // including the serverKey?
                try {
                    k.channel().close(); // including the server?
                } catch (final IOException ioe) {
                    throw new RuntimeException("failed to close " + k.channel(), ioe);
                }
            });
        }
    }
}
