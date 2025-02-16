package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.time.Duration;

@Slf4j
class Rfc864Tcp1Client {

    public static void main(final String... args) throws IOException {
        try (var client = new Socket()) {
            assert !client.isConnected();
            client.connect(_Constants.SERVER_ENDPOINT);
            assert client.isConnected();
            log.debug("connected to {} through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            client.shutdownOutput(); // IOException
            {
                client.setSoTimeout(1024);
            }
            __Utils.readQuitAndClose(client);
            for (int r; !client.isClosed(); ) {
                while ((r = client.getInputStream().read()) != -1) { // IOException
                    System.out.print((char) r);
                }
            }
        }
    }
}
