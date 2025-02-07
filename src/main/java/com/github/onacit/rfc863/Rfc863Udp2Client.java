package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp2Client {

    public static void main(final String... args) throws Exception {
        try (var selector = Selector.open();
             var client = DatagramChannel.open()) {
            _Rfc863Utils.readQuitAndClose(client);
            client.configureBlocking(false);
            final var registeredKey = client.register(selector, SelectionKey.OP_WRITE);
            final var buffer = ByteBuffer.allocate(_Rfc863Constants.UDP_BUF_LEN);
            while (client.isOpen()) {
                final var c = selector.select(0);
                assert c == 1;
                final var selectedKeys = selector.selectedKeys();
                assert selectedKeys.size() == 1 && selectedKeys.contains(registeredKey);
                for (final var i = selectedKeys.iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key.isWritable();
                    final var channel = key.channel();
                    assert channel == client;
                    ThreadLocalRandom.current().nextBytes(buffer.array());
                    buffer.clear()
                            .position(ThreadLocalRandom.current().nextInt(buffer.capacity() + 1))
                            .limit(buffer.position() + ThreadLocalRandom.current().nextInt(buffer.remaining() + 1));
                    final var bytes = ((DatagramChannel) channel).send(buffer, _Rfc863Constants.SERVER_ENDPOINT);
                    assert bytes >= 0;
                    assert !buffer.hasRemaining();
                    Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                }
            }
        }
    }
}
