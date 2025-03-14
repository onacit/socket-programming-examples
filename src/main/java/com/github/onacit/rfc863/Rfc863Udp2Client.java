package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp2Client extends _Rfc863Udp_Client {

    public static void main(final String... args) throws Exception {
        try (var client = DatagramChannel.open()) {
            __Utils.readQuitAndClose(true, client);
            final var src = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            while (client.isOpen()) {
                ThreadLocalRandom.current().nextBytes(src.array());
                src.clear()
                        .position(ThreadLocalRandom.current().nextInt(src.remaining() + 1))
                        .limit(src.position() + ThreadLocalRandom.current().nextInt(src.remaining() + 1));
                final var remaining = src.remaining();
                final int w;
                try {
                    w = client.send(src, _Constants.SERVER_ENDPOINT);
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
