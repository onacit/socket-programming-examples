package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Slf4j
class Rfc863Udp1Server {

    public static void main(final String... args) throws Exception {
        try (var server = new DatagramSocket(null)) {
            server.setReuseAddress(true);
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalSocketAddress());
            _Utils.readQuitAndClose(server);
            final DatagramPacket packet;
            {
                final var buffer = new byte[_Constants.UDP_BUF_LEN];
                packet = new DatagramPacket(buffer, buffer.length);
            }
            while (true) {
                try {
                    server.receive(packet);
                } catch (final IOException ioe) {
                    if (server.isClosed()) {
                        break;
                    }
                    log.error("failed to receive packet", ioe);
                }
                final var length = packet.getLength();
                log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", length),
                          packet.getSocketAddress());
            }
        }
    }
}
