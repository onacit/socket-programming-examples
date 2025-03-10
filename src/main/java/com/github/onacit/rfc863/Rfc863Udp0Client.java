package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp0Client {

    public static void main(final String... args) throws IOException {
        try (var client = new DatagramSocket(null)) { // -> close() throws IOException
            assert !client.isBound();
            assert !client.isConnected();
            final var buf = new byte[ThreadLocalRandom.current().nextInt(128)];
            ThreadLocalRandom.current().nextBytes(buf);
            final var packet = new DatagramPacket(buf, buf.length);
            packet.setSocketAddress(Rfc863Udp0Server.ENDPOINT);
            log.debug("sending {} byte(s) to {}", packet.getLength(), packet.getSocketAddress());
            client.send(packet); // IOException
        }
    }
}
