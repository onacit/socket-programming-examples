package com.github.onacit.rfc862;

import com.github.onacit.__Constants;
import com.github.onacit.__SocketUtils;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An {@link Rfc862Tcp$Client} client program uses {@link Socket}.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
class Rfc862Tcp1Client_Socket extends Rfc862Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new Socket()) { // -> close() -> IOException
            assert !client.isConnected();
            // ----------------------------------------------------------------------------- SO_REUSEADDR / SO_REUSEPORT
            __SocketUtils.SO_REUSEADDR(client); // IOException
            __SocketUtils.SO_REUSEPORT(client); // IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (_Constants.TCP_CLIENT_BIND) {
                assert !client.isBound();
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0));
                assert client.isBound();
                log.debug("bound to {}", client.getLocalSocketAddress());
            }
            // ------------------------------------------------------------------------------------------------- connect
            final var endpoint = __Utils.parseSocketAddress(_Constants.PORT, args).orElse(_Constants.SERVER_ENDPOINT);
            log.debug("endpoint: {}", endpoint);
            client.connect(endpoint); // IOException
            assert client.isConnected();
            assert client.isBound(); // !!!
            log.debug("connected to {}, through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            // --------------------------------------------------------------------- read '!quit' and close the <client>
            __Utils.readQuitAndClose(true, client);
            // -------------------------------------------------------------------- keep sending/receiving random octets
            for (int b, e; !client.isClosed(); ) {
                // send
                b = ThreadLocalRandom.current().nextInt(); // [-2,147,483,648..2,147,483,647] @@?
                client.getOutputStream().write(b); // IOException
                client.getOutputStream().flush(); // IOException
                // receive
                e = client.getInputStream().read(); // IOException
                if (e == -1) {
                    __Utils.logEof(client.getRemoteSocketAddress());
                    break;
                }
                assert e == (b & 0xFF); // [0..255]
                _Utils.logEchoed(e, client.getRemoteSocketAddress());
                // ----------------------------------------------------------------------------------------------- sleep
                if (_Constants.TCP_CLIENT_THROTTLE) {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(1024L) + 1024L); // InterruptedException
                }
            }
        }
    }
}
