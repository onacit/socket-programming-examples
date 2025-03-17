package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.StandardSocketOptions;

@Slf4j
class Rfc863Udp1Server_DatagramSocket extends _Rfc863Udp_Server {

    public static void main(final String... args) throws Exception {
        try (var server = new DatagramSocket(null)) {
            {
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uhe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uhe);
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                } catch (final UnsupportedOperationException uhe) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uhe);
                }
                server.setReuseAddress(true); // SocketException
            }
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
