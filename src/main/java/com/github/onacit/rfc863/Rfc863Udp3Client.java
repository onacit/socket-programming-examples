package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp3Client extends _Rfc863Udp_Client {

    public static void main(final String... args) throws Exception {
        try (var selector = Selector.open();
             var client = DatagramChannel.open()) {
            client.configureBlocking(false);
            final var clientKey = client.register(selector, SelectionKey.OP_WRITE);
            Thread.ofPlatform().name("write-op-setter").daemon(true).start(() -> {
                while (true) {
                    try {
                        Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                    } catch (final InterruptedException ie) {
                        clientKey.cancel();
                        selector.wakeup();
                    }
                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    selector.wakeup();
                }
            });
            __Utils.readQuitAndRun(true, () -> {
                clientKey.cancel();
                selector.wakeup();
            });
            final var src = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            while (clientKey.isValid()) {
                final var count = selector.select(0);
                assert count >= 0;
                final var keys = selector.selectedKeys();
                for (final var i = keys.iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    assert key.isWritable();
                    final var channel = key.channel();
                    assert channel == client;
                    ThreadLocalRandom.current().nextBytes(src.array());
                    src.clear()
                            .position(ThreadLocalRandom.current().nextInt(src.capacity() + 1))
                            .limit(src.position() + ThreadLocalRandom.current().nextInt(src.remaining() + 1));
                    final var w = ((DatagramChannel) channel).send(src, _Constants.SERVER_ENDPOINT);
                    assert w >= 0;
                    assert !src.hasRemaining();
//                    Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                    key.interestOpsAnd(~SelectionKey.OP_WRITE);
                }
            }
        }
    }
}
