package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;

@Slf4j
class Rfc862Tcp3Server_ServerSocketChannel_NonBlocking extends Rfc862Tcp$Server {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
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
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.socket().isBound();
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
            // ---------------------------------- read '!quit', cancel <serverKey>, close all clients, wakeup <selector>
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
                                throw new RuntimeException("failed to close " + k.channel(), ioe);
                            }
                        });
                selector.wakeup();
            });
            // ------------------------------------------------------------------ keep selecting, handling selected keys
            while (serverKey.isValid()) {
                final var count = selector.select(0); // IOException
                assert count >= 0;
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
                        // ------------------------------------------- register <client> to <selector> for the <OP_READ>
                        final var clientKey = client.register(
                                selector,              // <sel>
                                SelectionKey.OP_READ,  // <ops>
                                ByteBuffer.allocate(1) // <att>
                        ); // ClosedChannelException
                    }
                    // -------------------------------------------------------------------------------- readable -> read
                    if (key.isReadable()) { // CanceledKeyException
                        assert channel instanceof SocketChannel;
                        final var buffer = (ByteBuffer) key.attachment();
                        final var r = ((ReadableByteChannel) channel).read(buffer); // IOException
                        if (r == -1) {
                            key.cancel();
                            assert !key.isValid();
                            continue;
                        }
                        assert r >= 0; // why?
                        if (buffer.position() > 0) {
                            key.interestOpsOr(SelectionKey.OP_WRITE);
                        }
                    }
                    // -------------------------------------------------------------------------------- writable -> send
                    if (key.isWritable()) {
                        assert channel instanceof SocketChannel;
                        final var buffer = (ByteBuffer) key.attachment();
                        assert buffer.position() > 0;
                        buffer.flip();
                        assert buffer.hasRemaining();
                        final var w = ((WritableByteChannel) channel).write(buffer); // IOException
                        assert w > 0;
                        buffer.compact();
                        if (buffer.position() == 0) {
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                        }
                    }
                }
            }
        }
    }
}
