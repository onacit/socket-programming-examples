package com.github.onacit.rfc864;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc864Udp1Client_DatagramSocket extends Rfc864Udp$Client {

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
                // ------------------------------ now, we can't send packets to other than the connected remote address
                final var packet = new DatagramPacket(new byte[0], 0);
                final var address = new InetSocketAddress(__Constants.ANY_LOCAL, 60000);
                packet.setSocketAddress(address);
                try {
                    client.send(packet); // IOException
                    assert false : "shouldn't be here";
                } catch (final IllegalArgumentException iae) {
                    log.debug("expected; as connected; failed to send package to {}", address, iae);
                }
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
            }
            // ---------------------------------------------------------------------------------------- set <SO_TIMEOUT>
            client.setSoTimeout(Math.toIntExact(TimeUnit.SECONDS.toMillis(1L)));
            // ------------------------------------------------------------------ keep sending <packet> through <client>
            while (!client.isClosed()) {
                final var data = packet.getData();
                ThreadLocalRandom.current().nextBytes(data);
                packet.setLength(ThreadLocalRandom.current().nextInt(data.length + 1));
                packet.setSocketAddress(_Constants.SERVER_ENDPOINT);
                try {
                    client.send(packet); // IOException, SocketTimeoutException, PortUnreachableException
                    try {
                        client.receive(packet); // IOException, SocketTimeoutException, PortUnreachableException
                        final var address = packet.getSocketAddress(); // for debugging
                        System.out.print(new String(packet.getData(), 0, packet.getLength()));
                    } catch (final SocketTimeoutException ste) {
                        log.error("failed to receive", ste);
                    } catch (final PortUnreachableException pue) {
                        log.error("failed to receive", pue);
                        assert _Constants.UDP_CLIENT_CONNECT;
                    }
                } catch (final PortUnreachableException pue) {
                    log.error("failed to send", pue);
                    assert _Constants.UDP_CLIENT_CONNECT;
                }
                if (_Constants.THROTTLE) {
                    // just for the sanity
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                }
            }
        }
    }
}
