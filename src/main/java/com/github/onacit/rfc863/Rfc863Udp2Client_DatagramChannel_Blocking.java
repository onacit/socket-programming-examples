package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp2Client_DatagramChannel_Blocking extends Rfc863Udp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = DatagramChannel.open()) { // IOException
            assert client.isBlocking(); // !!!
            // ----------------------------------------------------------------------------- read 'quit', close <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------- prepare a datagram buffer
            final var src = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            // --------------------------------------------------------------------- keep sending <src> through <client>
            while (client.isOpen()) {
                __Utils.randomizeAvailableAndContent(src);
                final var w = client.send(src, _Constants.SERVER_ENDPOINT); // IOException
                assert w >= 0;
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
