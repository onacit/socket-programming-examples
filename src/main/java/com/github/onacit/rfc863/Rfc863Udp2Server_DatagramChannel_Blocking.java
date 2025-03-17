package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp2Server_DatagramChannel_Blocking extends _Rfc863Udp_Server {

    public static void main(final String... args) throws IOException {
        try (var server = DatagramChannel.open()) {
            {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    try {
                        server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                    } catch (final UnsupportedOperationException uhe) {
                        log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uhe);
                    }
                } else {
                    try {
                        server.socket().setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE); // IOException
                    } catch (final UnsupportedOperationException uhe) {
                        log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, uhe);
                    }
                }
                if (ThreadLocalRandom.current().nextBoolean()) {
                    try {
                        server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                    } catch (final UnsupportedOperationException uhe) {
                        log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uhe);
                    }
                } else {
                    try {
                        server.socket().setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); // IOException
                    } catch (final UnsupportedOperationException uhe) {
                        log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, uhe);
                    }
                }
                server.socket().setReuseAddress(true); // SocketException
            }
            if (ThreadLocalRandom.current().nextBoolean()) {
                server.bind(_Constants.SERVER_ENDPOINT_TO_BIND); // IOException
            } else {
                server.socket().bind(_Constants.SERVER_ENDPOINT_TO_BIND); // SocketException
            }
            log.info("bound to {}", server.getLocalAddress());
            __Utils.readQuitAndClose(true, server);
            assert server.isBlocking();
            final var dst = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            final var packet = new DatagramPacket(dst.array(), dst.array().length);
            while (server.isOpen()) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    final var address = server.receive(dst.clear()); // IOException
                    log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", dst.position()),
                              address);
                } else {
                    server.socket().receive(packet);
                    log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", dst.position()),
                              packet.getSocketAddress());
                }
            }
        }
    }
}
