package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp3Client {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open();
             var client = SocketChannel.open()) {
            client.configureBlocking(false);
            final SelectionKey clientKey;
            if (client.connect(_Constants.SERVER_ENDPOINT)) {
                log.debug("connected to {}", client.getRemoteAddress());
                clientKey = client.register(selector, SelectionKey.OP_WRITE);
            } else {
                clientKey = client.register(selector, SelectionKey.OP_CONNECT);
            }
            _Utils.readQuitAndCall(() -> {
                clientKey.cancel();
                assert !clientKey.isValid();
                selector.wakeup();
                return null;
            });
            for (final var src = ByteBuffer.allocate(1); clientKey.isValid(); ) {
                final var count = selector.select(0L); // a blocking call; may be awaken by .wakeup()
                assert count >= 0; // why not 1?
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    if (key.isConnectable()) { // CanceledKeyException
                        if (!client.finishConnect()) { // IOException
                            log.error("failed to finish connecting");
                            key.cancel();
                            break;
                        } else {
                            log.debug("connected to {}", client.getRemoteAddress());
                            key.interestOpsAnd(~SelectionKey.OP_CONNECT);
                            Thread.ofVirtual().start(() -> {
                                assert Thread.currentThread().isDaemon();
                                while (!Thread.currentThread().isInterrupted()) {
                                    key.interestOps(SelectionKey.OP_WRITE); // CanceledKeyException
                                    selector.wakeup();
                                    try {
                                        Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
                                    } catch (final InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        key.cancel();
                                        selector.wakeup();
                                    }
                                }
                            });
                        }
                    } else if (key.isWritable()) { // almost always true; CanceledKeyException
                        ThreadLocalRandom.current().nextBytes(src.array());
                        final var w = client.write(src.clear());
                        assert w == 1; // why?
                        key.interestOpsAnd(~SelectionKey.OP_WRITE);
//                        Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
                    }
                }
            }
        }
    }
}
