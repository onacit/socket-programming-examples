package com.github.onacit.rfc864;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc864Udp2Client_DatagramChannel_Blocking extends Rfc864Udp$Client {

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
                // ------------------------------- now, we can't send packets to other than the connected remote address
                final var target = new InetSocketAddress(__Constants.ANY_LOCAL, 60000);
                try {
                    final var w = client.send(ByteBuffer.allocate(0), target);
                    assert false : "shouldn't be here";
                } catch (final AlreadyConnectedException ace) {
                    log.debug("expected; as connected; failed to send package to {}", target, ace);
                }
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
            final var dst = src.slice();
            // ---------------------------------------------------------------------------------------- set <SO_TIMEOUT>
            // no effects!!!
            client.socket().setSoTimeout(Math.toIntExact(TimeUnit.SECONDS.toMillis(1L))); // SocketException
            // --------------------------------------------------------------------- keep sending <src> through <client>
            while (client.isOpen()) {
                __Utils.randomizeAvailableAndContent(src);
                try {
                    if (client.isConnected()) {
                        final var w = client.write(src);
                        assert w >= 0;
                    } else {
                        final var w = client.send(src, _Constants.SERVER_ENDPOINT); // IOException
                        assert w >= 0;
                    }
                    try {
                        if (client.isConnected()) {
                            final var r = client.read(dst.clear()); // IOException
                        } else {
                            final var address = client.receive(dst.clear());
                        }
                        System.out.print(StandardCharsets.UTF_8.decode(dst.flip()));
                    } catch (final PortUnreachableException pue) {
                        log.error("failed to receive", pue);
                    }
                } catch (final PortUnreachableException pue) {
                    log.error("failed to send", pue);
                }
                if (_Constants.THROTTLE) {
                    // just for the sanity
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                }
            }
        }
    }
}
