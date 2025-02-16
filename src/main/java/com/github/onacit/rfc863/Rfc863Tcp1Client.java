package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp1Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new Socket()) {
            assert !client.isConnected();
            client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert client.isConnected();
            log.debug("connected to {}, through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            {
                client.shutdownInput(); // IOException
                try {
                    client.getInputStream().read();
                    throw new AssertionError("should not reach here");
                } catch (final IOException ioe) {
                    // expected
                }
            }
            __Utils.readQuitAndClose(true, client);
            while (!client.isClosed()) {
                client.getOutputStream().write(ThreadLocalRandom.current().nextInt(256)); // [0..255] // IOException
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024));
            }
        }
    }
}
