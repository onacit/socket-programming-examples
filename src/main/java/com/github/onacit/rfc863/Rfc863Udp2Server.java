package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

@Slf4j
class Rfc863Udp2Server {

    public static void main(final String... args) throws Exception {
        try (var selector = Selector.open();
             var server = DatagramChannel.open()) {
            server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
            server.bind(_Rfc863Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress());
            server.configureBlocking(false);
            final var registeredKey = server.register(selector, SelectionKey.OP_READ);
            final var buffer = ByteBuffer.allocate(_Rfc863Constants.UDP_BUF_LEN);
            while (true) {
                final var c = selector.select(0);
                assert c == 1;
                final var selectedKeys = selector.selectedKeys();
                assert selectedKeys.size() == 1 && selectedKeys.contains(registeredKey);
                for (final var i = selectedKeys.iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key.isReadable();
                    final var channel = key.channel();
                    assert channel == server;
                    final var address = ((DatagramChannel) channel).receive(buffer.clear());
                    log.debug("discarding {} byte(s) received from {}", String.format("%1$04x", buffer.position()),
                              address);
                }
            }
        }
    }
}
