package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Slf4j
class Rfc863Udp1Server extends _Rfc863Udp_Server {

    public static void main(final String... args) throws Exception {
        try (var server = new DatagramSocket(null)) {
            server.setReuseAddress(true); // SocketException
            assert !server.isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // SocketException
            log.info("bound to {}", server.getLocalSocketAddress());
            __Utils.readQuitAndClose(true, server);
            final DatagramPacket packet;
            {
                final var buf = new byte[__Constants.UDP_PAYLOAD_MAX];
                packet = new DatagramPacket(buf, buf.length);
            }
            while (!server.isClosed()) {
                server.receive(packet); // IOException
                log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", packet.getLength()),
                          packet.getSocketAddress());
            }
        }
    }
}
