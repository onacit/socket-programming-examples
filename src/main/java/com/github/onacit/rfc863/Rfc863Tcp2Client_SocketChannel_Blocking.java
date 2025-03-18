package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp2Client_SocketChannel_Blocking extends Rfc863Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = SocketChannel.open()) {
            {
                assert !client.socket().isBound();
                assert !client.isConnected();
                assert client.isBlocking();
                final var connected = client.connect(_Constants.SERVER_ENDPOINT); // IOException
                assert connected;
                assert client.socket().isBound();
                assert client.isConnected();
                log.debug("connected to {}, through {}", client.getRemoteAddress(), client.getLocalAddress());
            }
            {
                __Utils.readQuitAndClose(true, client);
            }
            for (final var src = ByteBuffer.allocate(1); client.isOpen(); ) {
                ThreadLocalRandom.current().nextBytes(src.array());
                if (ThreadLocalRandom.current().nextBoolean()) {
                    final var w = client.write(src.clear()); // IOException
                    assert w == 1;
                } else {
                    client.socket().getOutputStream().write(src.get(0) & 0xFF); // IOException
                }
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
