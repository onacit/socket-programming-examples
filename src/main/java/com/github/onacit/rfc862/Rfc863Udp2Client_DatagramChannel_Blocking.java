package com.github.onacit.rfc862;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp2Client_DatagramChannel_Blocking extends Rfc863Udp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = DatagramChannel.open()) { // IOException
            assert client.isBlocking(); // !!!
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
            // ------------------------------------------------------------------------- read 'quit', close the <client>
            __Utils.readQuitAndCall(true, () -> {
                if (client.isConnected()) {
                    try {
                        client.disconnect(); // IOException
                        assert !client.isConnected();
                    } catch (final IOException ioe) {
                        log.error("failed to disconnect {}", client, ioe);
                    }
                }
                client.close(); // IOException
                assert !client.isOpen();
                return null;
            });
            // ------------------------------------------------------------------------------- prepare a datagram buffer
            final var src = ByteBuffer.allocate(__Constants.UDP_PAYLOAD_MAX);
            // --------------------------------------------------------------------- keep sending <src> through <client>
            while (client.isOpen()) {
                __Utils.randomizeAvailableAndContent(src);
                try {
                    final var w = client.send(src, _Constants.SERVER_ENDPOINT); // IOException
                    assert w >= 0;
                } catch (final PortUnreachableException pue) {
                    log.error("failed to send", pue);
                }
                // just for the sanity
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
