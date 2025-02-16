package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp2Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = SocketChannel.open()) {
            final var connected = client.connect(_Constants.SERVER_ENDPOINT);
            assert connected;
            log.debug("connected to {}, through {}", client.getRemoteAddress(), client.getLocalAddress());
            __Utils.readQuitAndClose(true, client);
            for (final var src = ByteBuffer.allocate(1); client.isOpen(); ) {
                ThreadLocalRandom.current().nextBytes(src.array());
                final var w = client.write(src.clear()); // IOException
                assert w >= 0; // why?
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
            }
        }
    }
}
