package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A minimal TCP client that connects to a server and sends a random byte to it.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Rfc863Tcp0Client {

    public static void main(final String... args) throws IOException {
        try (var client = new Socket()) {
            assert !client.isBound();
            assert !client.isConnected();
            client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert client.isBound();
            assert client.isConnected();
            log.debug("connected to {}, through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            client.getOutputStream().write(ThreadLocalRandom.current().nextInt(256)); // IOException
        }
    }
}
