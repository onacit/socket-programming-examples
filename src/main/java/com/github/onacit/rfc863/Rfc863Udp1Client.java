package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp1Client {

    public static void main(final String... args) throws Exception {
        try (var client = new DatagramSocket()) {
            _Utils.readQuitAndClose(client);
            final DatagramPacket packet;
            {
                final var buf = new byte[_Constants.UDP_BUF_LEN];
                packet = new DatagramPacket(buf, buf.length);
                packet.setSocketAddress(_Constants.SERVER_ENDPOINT);
            }
            while (!client.isClosed()) {
                ThreadLocalRandom.current().nextBytes(packet.getData());
                packet.setLength(ThreadLocalRandom.current().nextInt(packet.getData().length + 1));
                client.send(packet);
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
            }
        }
    }
}
