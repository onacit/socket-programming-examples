package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
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
                client.setReuseAddress(true); // SocketException
                client.bind(new InetSocketAddress(__Constants.HOST, 0)); // SocketException
                log.debug("bound to {}", client.getLocalSocketAddress());
                assert client.isBound();
            }
            __Utils.readQuitAndClose(true, client);
            final DatagramPacket packet;
            {
                final var buf = new byte[__Constants.UDP_PAYLOAD_MAX];
                packet = new DatagramPacket(buf, buf.length);
                packet.setSocketAddress(_Constants.SERVER_ENDPOINT);
            }
            while (!client.isClosed()) {
                ThreadLocalRandom.current().nextBytes(packet.getData());
                packet.setLength(ThreadLocalRandom.current().nextInt(packet.getData().length + 1));
                client.send(packet); // IOException
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
