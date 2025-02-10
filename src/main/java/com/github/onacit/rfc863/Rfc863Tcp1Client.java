package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp1Client {

    public static void main(final String... args) throws Exception {
        try (var client = new Socket()) {
            assert !client.isConnected();
            client.connect(_Rfc863Constants.SERVER_ENDPOINT);
            assert client.isConnected();
            log.debug("connected to {} through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            {
                client.shutdownInput(); // ???
                try {
                    final var b = client.getInputStream().read();
                    throw new AssertionError("should not reach here");
                } catch (final IOException ioe) {
                    // expected
                }
            }
            _Rfc863Utils.readQuitAndClose(client);
            while (!client.isClosed()) {
                client.getOutputStream().write(ThreadLocalRandom.current().nextInt(256)); // [0..255]
                Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
            }
        }
    }
}
