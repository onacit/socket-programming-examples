package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Slf4j
class Rfc864Tcp3Client {

    public static void main(final String... args) throws IOException {
        try (var selector = Selector.open();
             var client = SocketChannel.open()) {
            assert client.isBlocking();
            client.configureBlocking(false); // IOException
            assert !client.isBlocking();
            final SelectionKey clientKey;
            if (client.connect(_Constants.SERVER_ENDPOINT)) { // IOException
                log.debug("connected to {}", client.getRemoteAddress());
                clientKey = client.register(selector, SelectionKey.OP_WRITE); // ClosedChannelException
            } else {
                clientKey = client.register(selector, SelectionKey.OP_CONNECT); // ClosedChannelException
            }
            __Utils.readQuitAndCall(true, () -> {
                clientKey.cancel();
                assert !clientKey.isValid();
                selector.wakeup();
                return null;
            });
            for (final var dst = ByteBuffer.allocate(1); clientKey.isValid(); ) {
                final var count = selector.select(0L); // a blocking call; may be awaken by .wakeup()
                assert count >= 0; // why not 1?
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) { // ClosedSelectorException
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    if (key.isConnectable()) { // CanceledKeyException
                        if (!client.finishConnect()) { // ClosedChannelException, IOException
                            log.error("failed to finish connecting");
                            key.cancel();
                            break;
                        }
                        log.debug("connected to {}", client.getRemoteAddress()); // ClosedChannelException, IOException
                        key.interestOps(SelectionKey.OP_READ); // CancelledKeyException
                    } else if (key.isReadable()) { // CancelledKeyException
                        final var r = client.read(dst.position(0)); // ClosedChannelException, IOException
                        if (r == -1) {
                            key.cancel();
                            continue;
                        }
                        assert r == 1; // why?
                        System.out.print((char) dst.flip().get());
                    }
                }
            }
        }
    }
}
