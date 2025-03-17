package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp2Client_DatagramChannel_Blocking extends _Rfc863Udp_Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = DatagramChannel.open()) { // IOException
            assert client.isBlocking(); // !!!
            __Utils.readQuitAndClose(true, client);
            final var src = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            final var packet = new DatagramPacket(src.array(), src.capacity());
            packet.setSocketAddress(_Constants.SERVER_ENDPOINT);
            while (client.isOpen()) {
                __Utils.randomize(src.clear());
                if (ThreadLocalRandom.current().nextBoolean()) {
                    final var w = client.send(src, _Constants.SERVER_ENDPOINT); // IOException
                    assert w >= 0;
                    assert !src.hasRemaining();
                } else {
                    packet.setData(Arrays.copyOfRange(src.array(), src.position(), src.limit()));
                    client.socket().send(packet); // IOException
                }
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
