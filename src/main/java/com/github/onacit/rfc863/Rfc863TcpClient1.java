package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863TcpClient1 {

    public static void main(final String... args) throws Exception {
        try (var client = new Socket()) {
            client.setReuseAddress(true);
            client.connect(_Rfc863Constants.SERVER_ENDPOINT_TO_CONNECT);
            log.debug("connected to {} through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            _Rfc863Utils.readQuitAndClose(client);
            {
                client.shutdownInput(); // ???
                try {
                    final var r = client.getInputStream().read();
                    throw new AssertionError("should not reach here");
                } catch (final IOException ioe) {
                    // expected
                }
            }
            while (true) {
                client.getOutputStream().write(ThreadLocalRandom.current().nextInt(256)); // [0..255]
                Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1024)));
            }
        }
    }
}
