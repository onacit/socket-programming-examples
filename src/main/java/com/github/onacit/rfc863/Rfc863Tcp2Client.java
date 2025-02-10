package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp2Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = SocketChannel.open()) {
            final var connected = client.connect(_Rfc863Constants.SERVER_ENDPOINT);
            assert connected;
            log.debug("connected to {}", client.getRemoteAddress());
            _Rfc863Utils.readQuitAndClose(client);
            final var src = ByteBuffer.allocate(1);
            while (client.isOpen()) {
                ThreadLocalRandom.current().nextBytes(src.array());
                try {
                    final var w = client.write(src.clear());
                    assert w >= 0;
                } catch (final ClosedChannelException cce) {
                    assert !client.isOpen();
                    continue;
                }
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
            }
        }
    }
}
