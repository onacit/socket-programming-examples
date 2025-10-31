package com.github.onacit.rfc864;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

@Slf4j
class Rfc864Udp3Server_DatagramChannel_NonBlocking extends Rfc864Udp$Server {

    public static void main(final String... args) throws Exception {
        try (var selector = Selector.open();
             var server = DatagramChannel.open()) {
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
            server.socket().setReuseAddress(true); // SocketException
            // ---------------------------------------------------------------------------------------------------- bind
            assert !server.socket().isBound();
            server.bind(_Constants.SERVER_ENDPOINT_TO_BIND);
            assert server.socket().isBound();
            log.info("bound to {}", server.getLocalAddress()); // IOException
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert server.isBlocking(); // !!!
            server.configureBlocking(false); // IOException
            assert !server.isBlocking();
            // ----------------------------------------------------------- register <server> to <selector> for <OP_READ>
            final var serverKey = server.register(selector, SelectionKey.OP_READ);
            // ---------------------------------------------- read 'quit', cancel the <serverKey>, wakeup the <selector>
            __Utils.readQuitAndRun(true, () -> {
                serverKey.cancel();
                assert !serverKey.isValid();
                selector.wakeup();
            });
            // ---------------------------------------------------------------- prepare a buffer for receiving datagrams
//            final var dst = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            final var dst = ByteBuffer.allocate(0);
            final var generator = _Utils.newPatternGenerator();
            // ------------------------------------------------------------------ keep selecting, handling selected keys
            while (serverKey.isValid()) {
                // ---------------------------------------------------------------------------------------------- select
                final var count = selector.select(0); // IOException
                assert count == 0 || count == 1;
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == serverKey;
                    final var channel = key.channel();
                    assert channel == server;
                    // ----------------------------------------------------------------------------- readable -> receive
                    if (key.isReadable()) {
                        final var source = ((DatagramChannel) channel).receive(dst.clear()); // IOException
                        assert source != null; // why?
                        key.attach(source);
                        key.interestOpsOr(SelectionKey.OP_WRITE);
                        assert !key.isWritable(); // still
                    }
                    // -------------------------------------------------------------------------------- writable -> send
                    if (key.isWritable()) {
                        final var target = (SocketAddress) key.attachment();
                        final var w = ((DatagramChannel) channel).send(generator.buffer(), target);
                        assert w >= 0;
                        key.interestOpsAnd(~SelectionKey.OP_WRITE);
                        assert key.isWritable(); // still
                    }
                }
            }
        }
    }
}
