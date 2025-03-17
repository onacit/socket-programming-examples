package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

@Slf4j
class Rfc863Udp3Server_DatagramChannel_NonBlocking extends _Rfc863Udp_Server {

    public static void main(final String... args) throws Exception {
        try (var selector = Selector.open();
             var server = DatagramChannel.open()) {
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
            server.socket().setReuseAddress(true); // SocketException
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            log.info("bound to {}", server.getLocalAddress()); // IOException
            assert server.isBlocking();
            server.configureBlocking(false); // IOException
            assert !server.isBlocking();
            final var serverKey = server.register(selector, SelectionKey.OP_READ);
            __Utils.readQuitAndRun(true, () -> {
                serverKey.cancel();
                assert !serverKey.isValid();
                selector.wakeup();
            });
            for (final var dst = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX); serverKey.isValid(); ) {
                final var count = selector.select(0); // IOException
                assert count == 0 || count == 1;
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == serverKey;
                    final var channel = key.channel();
                    assert channel == server;
                    final var address = ((DatagramChannel) channel).receive(dst.clear()); // IOException
                    log.debug("discarding {} byte(s) received from {}", String.format("%1$5d", dst.position()),
                              address);
                }
            }
        }
    }
}
