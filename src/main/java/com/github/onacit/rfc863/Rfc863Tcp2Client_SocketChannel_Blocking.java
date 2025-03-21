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
        try (var client = SocketChannel.open()) { // IOException
            // ---------------------------------------------------------------------------------------------------- bind
            assert !client.socket().isBound();
            assert !client.isConnected();
            assert client.isBlocking();
            // ------------------------------------------------------------------------------------------------- connect
            final var connected = client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert connected;
            assert client.socket().isBound();
            assert client.isConnected();
            log.debug("connected to {}, through {}", client.getRemoteAddress(), client.getLocalAddress());
            // --------------------------------------------------------------------- read 'quit', and close the <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------- keep sending random bytes
            assert client.isBlocking(); // !!!
            for (final var src = ByteBuffer.allocate(1); client.isOpen(); ) {
                __Utils.randomize(src);
                final var w = client.write(src); // IOException
                assert src.capacity() == 0 || w >= 0;
                if (_Constants.THROTTLE) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                }
            }
        }
    }
}
