package com.github.onacit.rfc864;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Udp3Client_DatagramChannel_NonBlocking extends Rfc864Udp$Client {

    public static void main(final String... args) throws Exception {
        try (var selector = Selector.open();
             var client = DatagramChannel.open()) { // IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (_Constants.UDP_CLIENT_BIND) {
                assert !client.socket().isBound();
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0)); // IOException
                assert client.socket().isBound();
                log.debug("bound to {}", client.getLocalAddress()); // IOException
            }
            // -------------------------------------------------------------------------------------- connect (optional)
            if (_Constants.UDP_CLIENT_CONNECT) {
                assert !client.isConnected();
                client.connect(_Constants.SERVER_ENDPOINT); // IOException
                assert client.isConnected();
                log.debug("connected to {}", client.getRemoteAddress()); // IOException
            }
            // ---------------------------------------------------------------------------------- configure non-blocking
            assert client.isBlocking(); // !!!
            client.configureBlocking(false);
            assert !client.isBlocking();
            // ------------------------------------------------------------------------- register <client> to <selector>
            final var clientKey = client
                    .register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE); // ClosedChannelException
            // -------------------------------------------------- read 'quit', and cancel <clientKey>, wakeup <selector>
            __Utils.readQuitAndRun(true, () -> {
                clientKey.cancel();
                assert !clientKey.isValid();
                selector.wakeup();
            });
            // -------------------------------------------------------------------------------- prepare datagram buffers
//            final var src = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            final var src = ByteBuffer.allocate(0);
            final var dst = ByteBuffer.allocate(__Rfc864_Constants.UDP_SERVER_CHARACTERS_MAX);
            // ------------------------------------------------------------------ keep selecting, handling selected keys
            while (clientKey.isValid()) {
                final var count = selector.select(0);
                assert count >= 0;
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    // -------------------------------------------------------------------------------- writable -> send
                    if (key.isWritable()) {
                        __Utils.randomizeAvailableAndContent(src);
                        try {
                            final var w = ((DatagramChannel) channel)
                                    .send(src, _Constants.SERVER_ENDPOINT); // IOException
                            assert w >= 0;
                            assert !src.hasRemaining();
                        } catch (final PortUnreachableException pue) {
                            log.error("failed to send", pue);
                            assert _Constants.UDP_CLIENT_CONNECT;
                        }
                        if (_Constants.THROTTLE) {
                            key.interestOpsAnd(~SelectionKey.OP_WRITE);
                            assert key.isWritable(); // still
                            Thread.ofVirtual().name("write-op-setter").start(() -> {
                                try {
                                    Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                                    clientKey.interestOpsOr(SelectionKey.OP_WRITE);
                                } catch (final InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    clientKey.cancel();
                                }
                                selector.wakeup();
                            });
                        }
                    }
                    if (key.isReadable()) {
                        final var source = ((DatagramChannel) channel).receive(dst.clear()); // IOException
                        System.out.print(StandardCharsets.UTF_8.decode(dst.flip()));
                    }
                }
            }
        }
    }
}
