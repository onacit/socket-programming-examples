package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
class Rfc864Tcp2Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = SocketChannel.open()) {
            assert client.isBlocking(); // in blocking mode!!!
            final var connected = client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert connected;
            log.debug("connected to {}", client.getRemoteAddress()); // IOException
            {
                client.socket().setSoTimeout(1024);
            }
            __Utils.readQuitAndClose(true, client);
            for (final var dst = ByteBuffer.allocate(1); client.isOpen(); dst.position(0)) {
                assert dst.remaining() == 1;
                final var r = client.read(dst); // IOException
                if (r == -1) {
                    break;
                }
                assert r == 1; // why?
                System.out.print((char) dst.flip().get());
            }
        }
    }
}
