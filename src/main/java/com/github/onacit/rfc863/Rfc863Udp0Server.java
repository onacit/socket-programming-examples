package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp0Server {

    static final InetAddress ADDR = InetAddress.getLoopbackAddress();

    static final int PORT = 10000 + __RFC863_Constants.PORT;

    public static void main(final String... args) throws IOException {
        try (var server = new DatagramSocket(null)) {
            if (!server.getReuseAddress()) { // SocketException
                server.setReuseAddress(true); // SocketException
                assert server.getReuseAddress();
            }
            assert !server.isBound();
            server.bind(new InetSocketAddress(ADDR, PORT)); // SocketException
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            final var buf = new byte[ThreadLocalRandom.current().nextInt(128)];
            final var packet = new DatagramPacket(buf, buf.length);
            server.receive(packet); // IOException
            log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", packet.getLength()),
                      packet.getSocketAddress());
        }
    }
}
