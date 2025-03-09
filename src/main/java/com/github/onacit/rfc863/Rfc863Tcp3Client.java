package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp3Client extends _Rfc863Tcp_Client {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open();
             var client = SocketChannel.open()) {

            assert client.isBlocking();
            client.configureBlocking(false);
            assert !client.isBlocking();

            final SelectionKey clientKey;
            if (client.connect(_Constants.SERVER_ENDPOINT)) {
                log.debug("connected to {}, through {}", client.getRemoteAddress(), client.getLocalAddress());
                clientKey = client.register(selector, SelectionKey.OP_WRITE);
            } else {
                clientKey = client.register(selector, SelectionKey.OP_CONNECT);
            }

            __Utils.readQuitAndCall(true, () -> {
                clientKey.cancel();
                assert !clientKey.isValid();
                selector.wakeup();
                return null;
            });

            for (final var src = ByteBuffer.allocate(1); clientKey.isValid(); ) {
                final var count = selector.select(0L); // IOException
                assert count >= 0; // why not 1?
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    if (key.isConnectable()) {
                        if (!client.finishConnect()) { // IOException
                            log.error("failed to finish connecting");
                            key.cancel();
                            continue;
                        }
                        log.debug("connected to {}, through {}",
                                  client.getRemoteAddress(), // IOException
                                  client.getLocalAddress() // IOException
                        );
                        key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                        Thread.ofVirtual().start(() -> {
                            assert Thread.currentThread().isDaemon();
                            while (!Thread.currentThread().isInterrupted() && key.isValid()) {
                                key.interestOps(SelectionKey.OP_WRITE);
                                selector.wakeup();
                                try {
                                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                                } catch (final InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    key.cancel();
                                    selector.wakeup();
                                }
                            }
                        });
                    }
                    if (key.isWritable()) {
                        ThreadLocalRandom.current().nextBytes(src.array());
                        final var w = client.write(src.clear()); // IOException
                        assert w > 0; // why?
                        key.interestOpsAnd(~SelectionKey.OP_WRITE);
//                        Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                    }
                }
            }
        }
    }
}
