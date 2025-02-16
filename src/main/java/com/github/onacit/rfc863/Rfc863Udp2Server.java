package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;

@Slf4j
class Rfc863Udp2Server {

    public static void main(final String... args) throws Exception {
        try (var server = DatagramChannel.open()) {
            {
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
                } catch (final Exception e) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, e);
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
                } catch (final Exception e) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, e);
                }
            }
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress());
            __Utils.readQuitAndClose(true, server);
            final var dst = ByteBuffer.allocate(__Constants.UDP_LEN);
            while (server.isOpen()) {
                final SocketAddress a;
                try {
                    a = server.receive(dst.clear()); // blocking call
                } catch (final ClosedChannelException cce) {
                    assert !server.isOpen();
                    continue;
                }
                log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", dst.position()), a);
            }
        }
    }
}
