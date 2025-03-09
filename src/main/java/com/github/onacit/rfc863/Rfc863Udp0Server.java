package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp0Server {

    private static final InetAddress ADDR = InetAddress.getLoopbackAddress();

    private static final int PORT = 10000 + __RFC863_Constants.PORT;

    static final SocketAddress ENDPOINT = new InetSocketAddress(ADDR, PORT);

    public static void main(final String... args) throws Exception {
        try (var server = new DatagramSocket(null)) {

            if (!server.getReuseAddress()) {
                server.setReuseAddress(true);
                assert server.getReuseAddress();
            }

            assert !server.isBound();
            server.bind(ENDPOINT);
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
