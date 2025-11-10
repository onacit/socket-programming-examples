package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc862Tcp3Server_ServerSocketChannel_NonBlocking extends Rfc862Tcp$Server {

    private static final int CAPACITY_MAX = 1;

    static {
        assert CAPACITY_MAX > 0;
    }

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var server = ServerSocketChannel.open()) { // IOException
            assert !server.isBlocking();
            // ------------------------------------------------------------------------------- try to reuse address/port
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uoe) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uoe);
                // empty
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uoe) {
                log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uoe);
                // empty
            }
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
                        log.debug("accepted from {}, through {}",
                                  client.getRemoteAddress(),
                                  client.getLocalAddress() // IOException
                        );
                        // ---------------------------------------------------------------------- configure non-blocking
                        assert client.isBlocking();
                        client.configureBlocking(false); // IOException
                        assert !client.isBlocking();
                        // ----------------------------------------------- register <client> to <selector> for <OP_READ>
                        final var clientKey = client.register(
                                selector,
                                SelectionKey.OP_READ,
                                ByteBuffer.allocate(ThreadLocalRandom.current().nextInt(CAPACITY_MAX) + 11)
                        ); // ClosedChannelException
                    }
                    // -------------------------------------------------------------------------------- readable -> read
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
                        if (buffer.position() > 0) {
                            key.interestOpsAnd(~SelectionKey.OP_READ);
                            assert key.isReadable();
                            buffer.flip(); // limit -> position, position -> zero
                            key.interestOpsOr(SelectionKey.OP_WRITE);
                            assert !key.isWritable();
                        }
                    }
                    // -------------------------------------------------------------------------------- writable -> send
                    if (key.isWritable()) {
                        assert channel instanceof SocketChannel;
                        final var buffer = (ByteBuffer) key.attachment();
                        {
                            assert buffer.hasRemaining();
                        }
                        for (int p = buffer.position(); p < buffer.limit(); p++) {
                            _Utils.logEchoing(buffer.get(p), ((SocketChannel) channel).getRemoteAddress());
                        }
                        w = ((WritableByteChannel) channel).write(buffer); // IOException
                        assert w >= 0; // @@?
                        if (!buffer.hasRemaining()) {
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            assert key.isWritable();
                            buffer.compact();
                            assert buffer.position() == 0;
                            assert buffer.limit() == buffer.capacity();
                            assert buffer.hasRemaining();
                            key.interestOpsOr(SelectionKey.OP_READ);
                            assert !key.isReadable();
                        }
                    }
                }
            }
        }
    }
}
