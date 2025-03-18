package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.StandardSocketOptions;

@Slf4j
class Rfc863Udp1Server_DatagramSocket extends Rfc863Udp$Server {

    public static void main(final String... args) throws Exception {
        try (var server = new DatagramSocket(null)) {
            // reuse address/port --------------------------------------------------------------------------------------
            try {
                server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            try {
                server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
            } catch (final UnsupportedOperationException uhe) {
                // empty
            }
            server.setReuseAddress(true); // SocketException
            // bind ----------------------------------------------------------------------------------------------------
            assert !server.isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // SocketException
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            // read `quit`, and close the <server> ---------------------------------------------------------------------
            __Utils.readQuitAndClose(true, server);
            // prepare a package ---------------------------------------------------------------------------------------
            final DatagramPacket packet;
            {
                final var buf = new byte[__Constants.UDP_PAYLOAD_MAX];
                packet = new DatagramPacket(buf, buf.length);
            }
            // keep reading --------------------------------------------------------------------------------------------
            while (!server.isClosed()) {
                server.receive(packet); // IOException
                log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", packet.getLength()),
                          packet.getSocketAddress());
            }
        }
    }
}
