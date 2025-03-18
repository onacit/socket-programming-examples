package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
class Rfc864Tcp2Client_SocketChannel_Blocking {

    public static void main(final String... args) throws IOException {
        try (var client = SocketChannel.open()) { // IOException
            {
                assert client.isBlocking(); // !!!
            }
            {
                final var connected = client.connect(_Constants.SERVER_ENDPOINT); // IOException
                assert connected;
                log.debug("connected to {}", client.getRemoteAddress()); // IOException
            }
            {
                __Utils.readQuitAndClose(true, client);
            }
            for (final var dst = ByteBuffer.allocate(1); client.isOpen(); dst.position(0)) {
                if (client.read(dst) == -1) { // IOException
                    break;
                }
                System.out.print((char) dst.flip().get());
            }
        }
    }
}
