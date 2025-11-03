package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A TCP client that connects to a server and sends a random byte to it.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
class Rfc863Tcp1Client_Socket extends Rfc863Tcp$Client {

    /**
     * The main method of this program.
     *
     * @param args an array of command line arguments.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if interrupted while running.
     * @see <a
     * href="https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/Socket.html">java.net.Socket</a>
     */
    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new Socket()) { // -> close() -> IOException
            assert !client.isConnected();
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (_Constants.TCP_CLIENT_BIND_MANUALLY) {
                assert !client.isBound() : "client is already bound";
                assert client.getLocalSocketAddress() == null;
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0));
                assert client.isBound();
                assert client.getLocalSocketAddress() != null;
                log.debug("bound to {}", client.getLocalSocketAddress());
            }
            // ------------------------------------------------------------------------------------------------- connect
            assert !client.isConnected();
            client.connect(_Constants.SERVER_ENDPOINT, _Constants.TCP_CLIENT_CONNECT_TIMEOUT); // IOException
            assert client.isConnected();
            assert client.getRemoteSocketAddress() != null;
            assert client.isBound(); // !!!
            assert client.getLocalSocketAddress() != null;
            log.debug("connected to {}, through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            // ------------------------------------------------------------------------------- shutdown input (optional)
            if (_Constants.TCP_CLIENT_SHUTDOWN_INPUT) {
                log.debug("shutting down the input...");
                client.shutdownInput(); // IOException
                try {
                    final var b = client.getInputStream().read(); // IOException
                    assert false;
                } catch (final IOException ioe) {
                    // expected
                }
                try {
                    final var r = client.getInputStream().read(new byte[0]);
                    assert false;
                } catch (final IOException ioe) {
                    // expected
                }
                try {
                    final var r = client.getInputStream().read(new byte[1]);
                    assert false;
                } catch (final IOException ioe) {
                    // expected;
                }
            }
            // ------------------------------------------------------------------------ read `!quit`, and close <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------ keep sending random octets
            for (int b; !client.isClosed(); ) {
                b = ThreadLocalRandom.current().nextInt(256); // [0..255]
                client.getOutputStream().write(b); // IOException
                client.getOutputStream().flush(); // IOException
                if (_Constants.TCP_CLIENT_THROTTLE) {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(1024L) + 1024L); // InterruptedException
                }
            }
        }
    }
}
