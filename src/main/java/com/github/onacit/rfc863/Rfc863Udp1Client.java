package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp1Client {

    public static void main(final String... args) throws Exception {
        try (var client = new DatagramSocket()) {
            _Rfc863Utils.readQuitAndClose(client);
            final DatagramPacket packet;
            {
                final var buffer = new byte[_Rfc863Constants.UDP_BUF_LEN];
                packet = new DatagramPacket(buffer, buffer.length);
                packet.setSocketAddress(_Rfc863Constants.SERVER_ENDPOINT);
            }
            while (!client.isClosed()) {
                ThreadLocalRandom.current().nextBytes(packet.getData());
                packet.setLength(ThreadLocalRandom.current().nextInt(packet.getData().length + 1));
                client.send(packet);
                Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
            }
        }
    }
}
