package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
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
class Rfc863Tcp1Client_Socket extends _Rfc863Tcp_Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new Socket()) {

            assert !client.isBound();
            assert !client.isConnected();
            client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert client.isBound();
            assert client.isConnected();
            log.debug("connected to {}, through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());

            {
                client.shutdownInput(); // IOException
                try { // TODO: remove
                    client.getInputStream().read(); // IOException
                    throw new AssertionError("shouldn't be here");
                } catch (final IOException ioe) {
                    // expected
                }
            }

            __Utils.readQuitAndClose(true, client);

            while (!client.isClosed()) {
                client.getOutputStream().write(ThreadLocalRandom.current().nextInt(256)); // IOException
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
