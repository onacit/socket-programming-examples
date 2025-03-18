package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp3Client_DatagramChannel_NonBlocking extends Rfc863Udp$Client {

    public static void main(final String... args) throws Exception {
        try (var selector = Selector.open();
             var client = DatagramChannel.open()) { // IOException
            // confiture non-blocking ----------------------------------------------------------------------------------
            assert client.isBlocking(); // !!!
            client.configureBlocking(false);
            assert !client.isBlocking();
            // register <client> to the <selector> ---------------------------------------------------------------------
            final var clientKey = client.register(selector, 0); // ClosedChannelException
            // start a new thread periodically sets OP_WRITE -----------------------------------------------------------
            Thread.ofPlatform().name("write-op-setter").daemon(true).start(() -> {
                while (clientKey.isValid()) {
                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    selector.wakeup();
                    try {
                        Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
                    } catch (final InterruptedException ie) {
                        clientKey.cancel();
                        selector.wakeup();
                    }
                }
            });
            // read 'quit', and cancel the <clientKey>, wakeup the <selector> ------------------------------------------
            __Utils.readQuitAndRun(true, () -> {
                clientKey.cancel();
                selector.wakeup();
            });
            // keep selecting, handling selected keys ------------------------------------------------------------------
            for (final var src = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX); clientKey.isValid(); ) {
                final var count = selector.select(0);
                assert count >= 0;
                for (final var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    final var key = i.next();
                    assert key == clientKey;
                    final var channel = key.channel();
                    assert channel == client;
                    assert key.isWritable(); // !!!
                    // write -------------------------------------------------------------------------------------------
                    if (key.isWritable()) {
                        __Utils.randomize(src.clear());
                        final var w = ((DatagramChannel) channel).send(src, _Constants.SERVER_ENDPOINT); // IOException
                        assert w >= 0;
                        assert !src.hasRemaining();
                        key.interestOpsAnd(~SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}
