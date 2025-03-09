package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp3Server {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open();
             var server = ServerSocketChannel.open()) {
            {
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
                server.socket().setReuseAddress(true);
            }
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress());
            assert server.isBlocking();
            server.configureBlocking(false); // IOException
            final var serverKey = server.register(selector, SelectionKey.OP_ACCEPT); // ClosedChannelException
            __Utils.readQuitAndCall(true, () -> {
                serverKey.cancel();
                assert !serverKey.isValid();
                selector.wakeup();
                return null;
            });
            for (final var dst = ByteBuffer.allocate(1); serverKey.isValid(); ) {
                final var count = selector.select(0); // IOException
                assert count >= 0;
                for (var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) { // ClosedSelectorException
                    final var key = i.next();
                    final var channel = key.channel();
                    if (key.isAcceptable()) { // CanceledKeyException
                        assert channel == server;
                        final var client = ((ServerSocketChannel) channel).accept();
                        log.debug("accepted from {}", client.getRemoteAddress()); // IOException
                        client.shutdownInput(); // IOException
                        assert client.isBlocking();
                        client.configureBlocking(false); // IOException
                        final var clientKey = client.register(selector, 0); // ClosedChannelException
                        final var buffer = ByteBuffer.allocate(1);
                        clientKey.attach(buffer);
                        Thread.ofVirtual().start(() -> {
                            while (clientKey.isValid()) {
                                clientKey.interestOps(SelectionKey.OP_WRITE);
                                try {
                                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
                                } catch (final InterruptedException ie) {
                                    log.error("interrupted while sleeping", ie);
                                    Thread.currentThread().interrupt();
                                    clientKey.cancel();
                                }
                            }
                        });
                    }
                    if (key.isWritable()) { // CanceledKeyException
                        assert channel instanceof SocketChannel;
                        final var attachment = key.attachment();
                        assert attachment != null;
                        final var r = ((ReadableByteChannel) channel).read(dst.clear()); // IOException
                        if (r == -1) {
                            key.cancel();
                            assert !key.isValid();
                            continue;
                        }
                        assert r >= 0;
                        assert r > 0; // why?
                        log.debug("discarding 0x{} received from {}", String.format("%1$02X", dst.get(0)), attachment);
                    }
                }
            }
        }
    }
}
