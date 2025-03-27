package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp2Client_SocketChannel_Blocking {

    public static void main(final String... args) throws IOException {
        try (var client = SocketChannel.open()) { // IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (ThreadLocalRandom.current().nextBoolean()) {
                assert !client.socket().isBound();
                client.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0)); // IOException
                assert client.socket().isBound();
                log.debug("bound to {}", client.getLocalAddress()); // IOException
            }
            // ------------------------------------------------------------------------------------------------- connect
            assert !client.isConnected();
            final var connected = client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert connected;
            assert client.isConnected();
            log.debug("connected to {}", client.getRemoteAddress()); // IOException
            // --------------------------------------------------------------------- read `quit`, and close the <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------------------ keep receiving
            assert client.isBlocking(); // !!!!
            for (final var dst = ByteBuffer.allocate(1); client.isOpen(); dst.position(0)) {
                if (client.read(dst) == -1) { // IOException
                    break;
                }
                System.out.print((char) dst.flip().get());
            }
        }
    }
}
