package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863TcpClient1 {

    public static void main(final String... args) throws Exception {
        try (var client = new Socket()) {
            client.connect(_Rfc863Constants.ENDPOINT);
            log.debug("connected to {}", client.getRemoteSocketAddress());
            while (true) {
                client.getOutputStream().write(ThreadLocalRandom.current().nextInt(256));
                Thread.sleep(Duration.ofMillis(ThreadLocalRandom.current().nextInt(128)));
            }
        }
    }
}
