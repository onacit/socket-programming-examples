package com.github.onacit.rfc862;

import com.github.onacit.__SocketUtils;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

@Slf4j
class Rfc862Tcp3Server_ServerSocketChannel_NonBlocking extends Rfc862Tcp$Server {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var server = ServerSocketChannel.open()) { // IOException
            assert !server.isBlocking();
            // ------------------------------------------------------------------------------- try to reuse address/port
            __SocketUtils.SO_REUSEADDR(server);
            __SocketUtils.SO_REUSEPORT(server);
            // ---------------------------------------------------------------------------------------------------- bind
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            assert server.socket().isBound();
            log.info("bound to {}", server.getLocalAddress());
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert server.isBlocking();
            server.configureBlocking(false); // IOException
            assert !server.isBlocking();
            // --------------------------------------------------------- register <server> to <selector> for <OP_ACCEPT>
            final SelectionKey serverKey = server.register(
                    selector,              // <sel>
                    SelectionKey.OP_ACCEPT // <ops>
            ); // ClosedChannelException
            // ----------------------------- read '!quit', cancel <serverKey>, close all clients, and wake up <selector>
            __Utils.readQuitAndRun(true, () -> {
                serverKey.cancel();
                assert !serverKey.isValid();
                selector.keys()
                        .stream()
                        .filter(k -> k.channel() instanceof SocketChannel)
                        .forEach(k -> {
                            try {
                                k.channel().close();
                                assert !k.isValid();
                            } catch (final IOException ioe) {
                                log.error("failed to close {}", k.channel(), ioe);
                            }
                        });
                selector.wakeup();
            });
            // ------------------------------------------------------------------ keep selecting, handling selected keys
            // r: number of bytes read
            // w: number of bytes written
            for (int r, w; serverKey.isValid(); ) {
                final var count = selector.select(0); // IOException
                assert count >= 0; // why not positive?
                for (var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    final var channel = key.channel();
                    // ---------------------------------------------------------------------------- acceptable -> accept
                    if (key.isAcceptable()) {
                        assert channel instanceof ServerSocketChannel;
                        assert channel == server;
                        final var client = ((ServerSocketChannel) channel).accept(); // IOException
                        __Utils.logAccepted(client.getRemoteAddress(), client.getLocalAddress());
                        assert client.isBlocking();
                        // ---------------------------------------------------------------------- configure non-blocking
                        client.configureBlocking(false); // IOException
                        assert !client.isBlocking();
                        // ----------------------------------------------- register <client> to <selector> for <OP_READ>
                        final var clientKey = client.register(
                                selector,
                                SelectionKey.OP_READ,
                                ByteBuffer.allocate(_Utils.getTcpServerBufferCapacity())
                        ); // ClosedChannelException
                    }
                    // ----------------------------------------------------------------------------- readable -> receive
                    if (key.isReadable()) { // CanceledKeyException
                        assert channel instanceof SocketChannel;
                        final var buffer = (ByteBuffer) key.attachment();
                        r = ((ReadableByteChannel) channel).read(buffer); // IOException
                        if (r == -1) {
                            __Utils.logReceivedEof(((SocketChannel) channel).getRemoteAddress());
                            key.cancel();
                            assert !key.isValid();
                            continue;
                        }
                        assert r >= 0; // @@?
                        if (buffer.position() > 0) { // has remaining bytes to send
                            key.interestOpsOr(SelectionKey.OP_WRITE);
                            assert !key.isWritable();
                        }
                    }
                    // -------------------------------------------------------------------------------- writable -> send
                    if (key.isWritable()) {
                        assert channel instanceof SocketChannel;
                        final var buffer = (ByteBuffer) key.attachment();
                        buffer.flip();
                        assert buffer.hasRemaining();
                        for (int p = buffer.position(); p < buffer.limit(); p++) {
                            _Utils.logEchoing(buffer.get(p), ((SocketChannel) channel).getRemoteAddress());
                        }
                        w = ((WritableByteChannel) channel).write(buffer); // IOException
                        assert w >= 0; // @@?
                        buffer.compact();
                        if (buffer.position() == 0) { // no remaining bytes to send
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            assert key.isWritable();
                        }
                    }
                }
            }
        }
    }
}
