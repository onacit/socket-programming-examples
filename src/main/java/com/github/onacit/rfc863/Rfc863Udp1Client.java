package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import com.github.onacit.rfc768.__Rfc768_Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp1Client extends _Rfc863Udp_Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new DatagramSocket(null)) { // -> close() -> IOException

            assert !client.isBound();
            if (ThreadLocalRandom.current().nextBoolean()) {
                client.setReuseAddress(true);
                client.bind(new InetSocketAddress(__Constants.HOST, 0));
                log.debug("bound to {}", client.getLocalSocketAddress());
                assert client.isBound();
            }

            __Utils.readQuitAndClose(true, client);

            final DatagramPacket packet;
            {
                final var buf = new byte[
                        ThreadLocalRandom.current().nextInt(
                                __Rfc768_Constants.IPv4PseudoHeader.DATA_BYTES_MAX + 1)
                        ];
                final var length = buf.length;
                packet = new DatagramPacket(buf, length);
                packet.setSocketAddress(_Constants.SERVER_ENDPOINT);
            }

            while (!client.isClosed()) {
                packet.setLength(ThreadLocalRandom.current().nextInt(packet.getData().length + 1));
                client.send(packet); // IOException
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
