package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
class Rfc864Tcp1Client_Socket extends _Rfc864Tcp_Client {

    public static void main(final String... args) throws IOException {
        try (var client = new Socket()) {
            client.connect(_Constants.SERVER_ENDPOINT); // IOException
            log.debug("connected to {} through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            client.shutdownOutput(); // IOException
            __Utils.readQuitAndClose(true, client);
            for (int b; (b = client.getInputStream().read()) != -1; ) { // IOException
                System.out.print((char) b);
            }
        }
    }
}
