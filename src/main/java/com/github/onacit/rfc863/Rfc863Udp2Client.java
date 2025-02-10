package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp2Client {

    public static void main(final String... args) throws Exception {
        try (var client = DatagramChannel.open()) {
            _Rfc863Utils.readQuitAndClose(client);
            final var src = ByteBuffer.allocate(_Rfc863Constants.UDP_BUF_LEN);
            while (client.isOpen()) {
                ThreadLocalRandom.current().nextBytes(src.array());
                src.clear()
                        .position(ThreadLocalRandom.current().nextInt(src.remaining() + 1))
                        .limit(src.position() + ThreadLocalRandom.current().nextInt(src.remaining() + 1));
                final var remaining = src.remaining();
                final int w;
                try {
                    w = client.send(src, _Rfc863Constants.SERVER_ENDPOINT);
                } catch (final ClosedChannelException cce) {
                    assert !client.isOpen();
                    continue;
                }
                assert w == remaining;
                assert !src.hasRemaining();
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
            }
        }
    }
}
