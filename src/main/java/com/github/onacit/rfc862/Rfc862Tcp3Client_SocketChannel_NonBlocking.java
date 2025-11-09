package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc862Tcp3Client_SocketChannel_NonBlocking extends Rfc862Tcp$Client {

    private static final int CAPACITY_MAX = 1;

    static {
        assert CAPACITY_MAX > 0;
    }

    /**
     * .
     *
     * @param args an array of command line arguments.
     * @throws IOException if an I/O error occurs.
     */
    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open(); // IOException
             var client = SocketChannel.open()) { // IOException
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert client.isBlocking();
            client.configureBlocking(false);
            assert !client.isBlocking();
            // ----------------------------------------------------- try to connect, and register <client> to <selector>
            final SelectionKey clientKey;
            final var remote = __Utils.parseSocketAddress(_Constants.PORT, args).orElse(_Constants.SERVER_ENDPOINT);
            if (client.connect(remote)) { // IOException
                log.debug("connected, immediately, to {}, through {}",
                          client.getRemoteAddress(), // IOException
                          client.getLocalAddress() // IOException
                );
                // ------------------------------------------------------------------ register <clientKey> to <selector>
                clientKey = client.register(selector, SelectionKey.OP_WRITE); // ClosedChannelException
                // -------------------------------------------------------------------------------- attach a byte buffer
                final var buffer = ByteBuffer.allocate(ThreadLocalRandom.current().nextInt(CAPACITY_MAX) + 1);
                assert buffer.capacity() > 0;
                assert buffer.hasArray();
                buffer.position(buffer.limit());
                clientKey.attach(buffer);
            } else {
                log.debug("not (immediately) connected. registering for <OP_CONNECT>...");
                clientKey = client.register(selector, SelectionKey.OP_CONNECT); // ClosedChannelException
                assert !clientKey.isConnectable();
            }
            // ------------------------------------------------ read '!quit', cancel <clientKey>, and wake up <selector>
            __Utils.readQuitAndRun(
                    true,
                    () -> {
                        clientKey.cancel();
                        assert !clientKey.isValid();
                        selector.wakeup();
                    }
            );
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
                        __Utils.logConnected(
                                client.getRemoteAddress(), // IOException
                                client.getLocalAddress() // IOException
                        );
                        // -------------------------------------------------------------------------- unset <OP_CONNECT>
                        key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                        assert key.isConnectable();
                        // ------------------------------------------------------------------------ attach a byte buffer
                        final var buffer = ByteBuffer.allocate(ThreadLocalRandom.current().nextInt(CAPACITY_MAX) + 1);
                        assert buffer.capacity() > 0;
                        assert buffer.hasArray();
                        buffer.position(buffer.limit());
                        clientKey.attach(buffer);
                        // ------------------------------------------------------------------------------ set <OP_WRITE>
                        key.interestOpsOr(SelectionKey.OP_WRITE);
                        assert !clientKey.isWritable();
                    }
                    // -------------------------------------------------------------------------------- writable -> send
                    if (key.isWritable()) {
                        final var buffer = (ByteBuffer) key.attachment();
                        assert buffer.capacity() > 0;
                        assert buffer.hasArray();
                        if (!buffer.hasRemaining()) {
                            ThreadLocalRandom.current().nextBytes(buffer.array());
                            buffer.clear();
                            assert buffer.position() == 0;
                            assert buffer.limit() == buffer.capacity();
                            assert buffer.hasRemaining();
                        }
                        final var w = ((WritableByteChannel) channel).write(buffer); // IOException
                        assert w >= 0; // why?
                        if (w > 0) {
                            clientKey.interestOpsAnd(~SelectionKey.OP_WRITE);
                            assert clientKey.isWritable();
                            clientKey.interestOpsOr(SelectionKey.OP_READ);
                            assert !clientKey.isReadable();
                            buffer.flip(); // limit -> position, position -> zero
                            assert buffer.hasRemaining();
                        }
                    }
                    // ----------------------------------------------------------------------------- readable -> receive
                    if (key.isReadable()) {
                        final var buffer = (ByteBuffer) key.attachment();
                        assert buffer != null;
                        assert buffer.capacity() > 0;
                        assert buffer.hasRemaining();
                        final var r = ((ReadableByteChannel) channel).read(buffer);
                        if (r == -1) {
                            __Utils.logReceivedEof(((SocketChannel) channel).getRemoteAddress());
                            key.cancel();
                            assert !key.isValid();
                            continue;
                        }
                        assert r >= 0;
                        for (int p = buffer.position() - r; p < buffer.position(); p++) {
                            log.debug("echoed, {}, back from {}", __Utils.formatOctet(buffer.get(p)),
                                      ((SocketChannel) channel).getRemoteAddress());
                        }
                        if (!buffer.hasRemaining()) {
                            key.interestOpsAnd(~SelectionKey.OP_READ);
                            if (_Constants.TCP_CLIENT_THROTTLE) {
                                Thread.ofVirtual().name("write-op-setter").start(() -> {
                                    try {
                                        Thread.sleep(ThreadLocalRandom.current().nextInt(1024) + 1024);
                                    } catch (final InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        clientKey.cancel();
                                        assert !clientKey.isValid();
                                        return;
                                    }
                                    key.interestOpsOr(SelectionKey.OP_WRITE);
                                    selector.wakeup();
                                });
                            }
                        }
                    }
                }
            }
        }
    }
}
