package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc864Tcp1Client_Socket extends Rfc864Tcp$Client {

    public static void main(final String... args) throws IOException {
        try (var client = new Socket()) {
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (ThreadLocalRandom.current().nextBoolean()) {
                assert !client.isBound();
                client.bind(new java.net.InetSocketAddress(InetAddress.getLocalHost(), 0));
                assert client.isBound();
                log.debug("bound to {}", client.getLocalSocketAddress());
            }
            // ------------------------------------------------------------------------------------------------- connect
            client.connect(_Constants.SERVER_ENDPOINT); // IOException
            log.debug("connected to {} through {}", client.getRemoteSocketAddress(),
                      client.getLocalSocketAddress());
            // ------------------------------------------------------------------------------ shutdown output (optional)
            client.shutdownOutput(); // IOException
            // ------------------------------------------------------------------------- read `quit`, and close <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------------------ keep receiving
            for (int b; (b = client.getInputStream().read()) != -1; ) { // IOException
                System.out.print((char) b);
            }
        }
    }
}
