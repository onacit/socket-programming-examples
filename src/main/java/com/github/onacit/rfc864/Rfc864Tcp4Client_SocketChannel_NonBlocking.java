package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Slf4j
class Rfc864Tcp4Client_SocketChannel_NonBlocking {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open();
             var client = SocketChannel.open()) { // IOException
            // ---------------------------------------------------------------------------------- configure non-blocking
            // configure non-blocking
            assert client.isBlocking();
            client.configureBlocking(false); // IOException
            assert !client.isBlocking();
            // ----------------------------------------------------------------------------------------- -try to connect
            assert !client.isRegistered();
            final SelectionKey clientKey;
            if (client.connect(_Constants.SERVER_ENDPOINT)) { // IOException
                // ----------------------------- (immediately) connected, register <client> to <selector> for <OP_WRITE>
                log.debug("connected to {}", client.getRemoteAddress()); // IOException
                clientKey = client.register(selector, SelectionKey.OP_WRITE); // ClosedChannelException
            } else {
                // ----------------------- not (immediately) connected, register <client> to <selector> for <OP_CONNECT>
                clientKey = client.register(selector, SelectionKey.OP_CONNECT); // ClosedChannelException
            }
            assert client.isRegistered();
            // -------------------------------------------------- read `quit`, cancel <clientKey>, and wakeup <selector>
            __Utils.readQuitAndRun(true, () -> {
                clientKey.cancel();
                assert !clientKey.isValid();
                selector.wakeup();
            });
            // ----------------------------------------------------------- keep selecting keys, and handle selected keys
            for (final var dst = ByteBuffer.allocate(1); clientKey.isValid(); ) {
                // ---------------------------------------------------------------------------------------------- select
                final var count = selector.select(0L); // IOException
                assert count >= 0; // why not 1?
                // ---------------------------------------------------------------------------------------------- handle
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    // ----------------------------------------------------------------------------------------- connect
                    if (key.isConnectable()) {
                        final var connected = client.finishConnect(); // IOException
                        assert connected;
                        log.debug("connected to {}", client.getRemoteAddress()); //  IOException
                        key.interestOpsAnd(~SelectionKey.OP_CONNECT); // not interested in connecting
                        assert key.isConnectable(); // still
                        key.interestOpsOr(SelectionKey.OP_READ); // now interested in reading
                        assert !key.isReadable(); // still
                    }
                    // -------------------------------------------------------------------------------------------- read
                    if (key.isReadable()) {
                        final var r = client.read(dst.position(0)); // IOException
                        if (r == -1) {
                            key.cancel();
                            assert !key.isValid();
                        } else {
                            assert r == 1; // why?
                            System.out.print((char) dst.flip().get());
                        }
                    }
                }
            }
        }
    }
}
