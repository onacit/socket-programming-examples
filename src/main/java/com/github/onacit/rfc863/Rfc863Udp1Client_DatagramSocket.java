package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Udp1Client_DatagramSocket extends Rfc863Udp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new DatagramSocket(null)) { // -> close() -> IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            assert !client.isBound();
            if (_Constants.UDP_CLIENT_BIND) {
                client.setReuseAddress(true); // SocketException // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0)); // IOException
                assert client.isBound();
                log.debug("bound to {}", client.getLocalSocketAddress());
            }
            // -------------------------------------------------------------------------------------- connect (optional)
            assert !client.isConnected();
            if (_Constants.UDP_CLIENT_CONNECT) {
                client.connect(_Constants.SERVER_ENDPOINT); // SocketException
                assert client.isConnected();
                log.debug("connected to {}", client.getRemoteSocketAddress());
            }
            // --------------------------------------------------------------------- read 'quit', and close the <client>
            __Utils.readQuitAndCall(true, () -> {
                if (client.isConnected()) {
                    try {
                        client.disconnect(); // UncheckedIOException
                        assert !client.isConnected();
                    } catch (final UncheckedIOException uioe) {
                        log.error("failed to disconnect {}", client, uioe);
                    }
                }
                client.close();
                assert client.isClosed();
                return null;
            });
            // ---------------------------------------------------------------------------------------- prepare a packet
            final DatagramPacket packet;
            {
                final var buf = new byte[__Constants.UDP_PAYLOAD_MAX];
                packet = new DatagramPacket(buf, buf.length);
                packet.setSocketAddress(_Constants.SERVER_ENDPOINT);
            }
            // ------------------------------------------------------------------ keep sending <packet> through <client>
            while (!client.isClosed()) {
                final var data = packet.getData();
                ThreadLocalRandom.current().nextBytes(data);
                final var length = ThreadLocalRandom.current().nextInt(data.length + 1);
                packet.setLength(length);
                try {
                    client.send(packet); // IOException
                } catch (final PortUnreachableException pue) {
                    log.error("failed to send", pue);
                }
                // just for the sanity
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
