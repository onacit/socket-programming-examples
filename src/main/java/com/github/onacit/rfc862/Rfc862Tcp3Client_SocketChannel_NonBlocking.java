package com.github.onacit.rfc862;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
class Rfc862Tcp3Client_SocketChannel_NonBlocking extends Rfc862Tcp$Client {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var client = SocketChannel.open()) { // IOException
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert client.isBlocking();
            client.configureBlocking(false);
            assert !client.isBlocking();
            // --------------------------------------------------------------------------------- prepare a list of lines
            final var lines = new CopyOnWriteArrayList<String>();
            // --------------------------------------------- try to connect, and register the <client> to the <selector>
            final SelectionKey clientKey;
            final var remote = __Utils.parseSocketAddress(_Constants.PORT, args).orElse(_Constants.SERVER_ENDPOINT);
            if (client.connect(remote)) { // IOException
                log.debug("connected, immediately, to {}, through {}",
                          client.getRemoteAddress(), // IOException
                          client.getLocalAddress() // IOException
                );
                // ------------------------------------------------------------------ register <clientKey> to <selector>
                clientKey = client.register(selector, 0); // ClosedChannelException
                // ------------------------------------------------- read '!quit', cancel <clientKey>, wakeup <selector>
                __Utils.readQuitAndRun(
                        true,
                        () -> {
                            clientKey.cancel();
                            assert !clientKey.isValid();
                            selector.wakeup();
                        },
                        l -> {
                            lines.add(l);
                            clientKey.interestOpsOr(SelectionKey.OP_WRITE);
                            assert !clientKey.isWritable(); // still
                            selector.wakeup();
                        }
                );
            } else {
                log.debug("not, immediately, connected");
                clientKey = client.register(selector, SelectionKey.OP_CONNECT); // ClosedChannelException
            }
            // ------------------------------------------------------------------------------- keep sending random bytes
            while (clientKey.isValid()) {
                // ---------------------------------------------------------------------------------------------- select
                final var count = selector.select(0L); // IOException
                assert count >= 0; // why not 1?
                // -------------------------------------------------------------------------------- handle selected keys
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    // ---------------------------------------------------------------- connectable -> finish connecting
                    if (key.isConnectable()) {
                        final var finished = client.finishConnect(); // IOException
                        assert finished; // why?
                        log.debug("connected to {}, through {}",
                                  client.getRemoteAddress(), // IOException
                                  client.getLocalAddress() // IOException
                        );
                        // -------------------------------------------------------------------------- unset <OP_CONNECT>
                        key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                        // ---------------------------------------------------------------- start new thread reads lines
                        __Utils.readQuitAndRun(
                                true,
                                () -> {
                                    clientKey.cancel();
                                    assert !clientKey.isValid();
                                    selector.wakeup();
                                },
                                l -> {
                                    lines.add(l);
                                    clientKey.interestOpsOr(SelectionKey.OP_WRITE);
                                    assert !clientKey.isWritable(); // still
                                    selector.wakeup();
                                }
                        );
                    }
                    // -------------------------------------------------------------------------------- writable -> send
                    if (key.isWritable()) {
                        var buffer = (ByteBuffer) key.attachment();
                        if (buffer == null) {
                            assert !lines.isEmpty();
                            buffer = ByteBuffer.wrap(
                                    (lines.removeFirst() + System.lineSeparator()).getBytes()
                            );
                            key.attach(buffer);
                        } else {
                            assert buffer.hasRemaining();
                        }
                        final var w = ((WritableByteChannel) channel).write(buffer); // IOException
                        assert w >= 0; // why?
                        if (!buffer.hasRemaining()) {
                            buffer.clear();
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            assert key.isWritable(); // still
                            key.interestOpsOr(SelectionKey.OP_READ);
                            assert !key.isReadable();
                        }
                    }
                    // ----------------------------------------------------------------------------- readable -> receive
                    if (key.isReadable()) {
                        final var buffer = (ByteBuffer) key.attachment();
                        assert buffer != null;
                        assert buffer.hasRemaining();
                        final var r = ((ReadableByteChannel) channel).read(buffer);
                        if (r == -1) {
                            key.cancel();
                            assert !key.isValid();
                        } else {
                            assert r >= 0;
                            if (!buffer.hasRemaining()) {
                                System.out.print(__Constants.CHARSET.decode(buffer.flip()));
                                key.attach(null);
                                key.interestOpsAnd(~SelectionKey.OP_READ);
                                if (!lines.isEmpty()) {
                                    key.interestOpsOr(SelectionKey.OP_WRITE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
