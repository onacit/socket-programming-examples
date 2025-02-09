package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp2Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var selector = Selector.open();
             var client = SocketChannel.open()) {
            client.configureBlocking(false);
            final SelectionKey clientKey;
            if (client.connect(_Rfc863Constants.SERVER_ENDPOINT)) {
                log.debug("connected to {}", client.getRemoteAddress());
                clientKey = client.register(selector, SelectionKey.OP_WRITE);
            } else {
                clientKey = client.register(selector, SelectionKey.OP_CONNECT);
            }
            _Rfc863Utils.readQuitAndCall(() -> {
                clientKey.cancel();
                selector.wakeup();
                return null;
            });
            final var buffer = ByteBuffer.allocate(1);
            while (clientKey.isValid()) {
                final var count = selector.select(0L); // blocking call
                assert count >= 0; // why not 1?
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    if (key.isConnectable()) {
                        if (client.finishConnect()) {
                            log.debug("connected to {}", client.getRemoteAddress());
                            key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                            Thread.ofVirtual().start(() -> {
                                while (!Thread.currentThread().isInterrupted()) {
                                    key.interestOps(SelectionKey.OP_WRITE);
                                    selector.wakeup();
                                    try {
                                        Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                                    } catch (final InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            });
                        } else {
                            log.error("failed to connect to {}", _Rfc863Constants.SERVER_ENDPOINT);
                            key.cancel();
                        }
                    } else if (key.isWritable()) { // almost always true
                        ThreadLocalRandom.current().nextBytes(buffer.array());
                        final var w = client.write(buffer.clear());
                        assert w == 1;
                        key.interestOpsAnd(~SelectionKey.OP_WRITE);
//                        Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                    }
                }
            }
        }
    }
}
