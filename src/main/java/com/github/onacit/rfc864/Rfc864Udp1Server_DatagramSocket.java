package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.StandardSocketOptions;

@Slf4j
class Rfc864Udp1Server_DatagramSocket extends Rfc864Udp$Server {

    public static void main(final String... args) throws Exception {
        try (var server = new DatagramSocket(null)) {
            // ------------------------------------------------------------------------------- try to reuse address/port
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
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // SocketException
            assert server.isBound();
            log.info("bound to {}", server.getLocalSocketAddress());
            // ------------------------------------------------------------------------- read `quit`, and close <server>
            __Utils.readQuitAndClose(true, server);
            // ---------------------------------------------------------------------------------------- prepare a packet
            final DatagramPacket packet;
            {
                final var buf = new byte[_Constants.UDP_DATA_LENGTH];
                packet = new DatagramPacket(buf, buf.length);
            }
            // --------------------------------------------------------------------------- keep sending through <server>
            final var generator = _Utils.newPatternGenerator();
            while (!server.isClosed()) {
                server.receive(packet); // IOException
                final var buffer = generator.buffer();
                System.arraycopy(buffer.array(), 0, packet.getData(), 0, buffer.remaining());
                packet.setLength(buffer.remaining());
                server.send(packet);
            }
        }
    }
}
