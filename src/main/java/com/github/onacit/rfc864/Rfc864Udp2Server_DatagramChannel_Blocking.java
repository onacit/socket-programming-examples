package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@Slf4j
class Rfc864Udp2Server_DatagramChannel_Blocking extends Rfc864Udp$Server {

    public static void main(final String... args) throws IOException {
        try (var server = DatagramChannel.open()) {
            assert server.isBlocking(); // !!!
            // ------------------------------------------------------------------------------- try to reuse address/port
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
            server.socket().setReuseAddress(true); // SocketException
            // ---------------------------------------------------------------------------------------------------- bind
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // IOException
            log.info("bound to {}", server.getLocalAddress());
            // --------------------------------------------------------------------- read 'quit', and close the <server>
            __Utils.readQuitAndClose(true, server);
            // -------------------------------------------------------------------- prepare a datagram buffer to receive
//            final var dst = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            final var dst = ByteBuffer.allocate(0);
            final var generator = _Utils.newPatternGenerator();
            // ------------------------------------------------------------- keep receiving and sending through <server>
            while (server.isOpen()) {
                // --------------------------------------------------------------------------------------------- receive
                final var source = server.receive(dst.clear()); // IOException
                assert source != null;
                // ------------------------------------------------------------------------------------------------ send
                final var w = server.send(generator.buffer(), source); // IOException
                assert w >= 0;
            }
        }
    }
}
