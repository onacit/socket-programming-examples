package com.github.onacit.rfc862;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A minimal TCP client that connects to a server and sends a random byte to it.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Rfc863Tcp1Client_Socket extends Rfc862Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new Socket()) { // -> close() -> IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (_Constants.TCP_CLIENT_BIND) {
                assert !client.isBound();
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0));
                assert client.isBound();
                log.debug("bound to {}", client.getLocalSocketAddress());
            }
            // ------------------------------------------------------------------------------------------------- connect
            assert !client.isConnected();
            client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert client.isConnected();
            assert client.isBound(); // !!!
            log.debug("connected to {}, through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            // ------------------------------------------------------------------------- read `quit`, and close <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------- keep sending random bytes
            while (!client.isClosed()) {
                client.getOutputStream().write(ThreadLocalRandom.current().nextInt(256)); // IOException
                if (_Constants.THROTTLE) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                }
            }
        }
    }
}
