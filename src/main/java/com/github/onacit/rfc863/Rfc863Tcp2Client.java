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
            final SelectionKey clientKey = client.register(selector, 0);
            final boolean connected = client.connect(_Rfc863Constants.SERVER_ENDPOINT);
            if (connected) {
                log.debug("connected to {}", client.getRemoteAddress());
                clientKey.interestOps(SelectionKey.OP_WRITE);
            } else {
                clientKey.interestOps(SelectionKey.OP_CONNECT);
            }
            _Rfc863Utils.readQuitAndCall(() -> {
                clientKey.cancel();
                selector.wakeup();
                return null;
            });
            final var buffer = ByteBuffer.allocate(1);
            while (clientKey.isValid()) {
                selector.select(0L);
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    if (key.isConnectable()) {
                        if (client.finishConnect()) {
                            log.debug("connected to {}", client.getRemoteAddress());
                            clientKey.interestOps(SelectionKey.OP_WRITE);
                        } else {
                            log.error("failed to connect to {}", _Rfc863Constants.SERVER_ENDPOINT);
                            key.cancel();
                        }
                    } else if (key.isWritable()) {
                        ThreadLocalRandom.current().nextBytes(buffer.array());
                        final var w = client.write(buffer.clear());
                        assert w == 1;
                        Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                    }
                }
            }
        }
    }
}
