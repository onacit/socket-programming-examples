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
            _Rfc863Utils.readQuitAndCall(() -> {
                registeredKey.cancel();
                log.debug("valid: {}", registeredKey.isValid());
                selector.wakeup();
                return null;
            });
            final var buffer = ByteBuffer.allocate(_Rfc863Constants.UDP_BUF_LEN);
            while (registeredKey.isValid()) {
                final var numberOfKeySelected = selector.select(0);
                if (numberOfKeySelected == 0) {
                    continue;
                }
                final var selectedKeys = selector.selectedKeys();
                assert selectedKeys.size() == 1 && selectedKeys.contains(registeredKey);
                for (final var i = selectedKeys.iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key.isReadable();
                    final var channel = key.channel();
                    assert channel == server;
                    final var address = ((DatagramChannel) channel).receive(buffer.clear());
                    log.debug("discarding 0x{} byte(s) received from {}", String.format("%1$04x", buffer.position()),
                              address);
                }
            }
        }
    }
}
