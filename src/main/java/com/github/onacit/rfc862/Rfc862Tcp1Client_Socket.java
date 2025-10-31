package com.github.onacit.rfc862;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

import static com.github.onacit.__SocketUtils.SO_REUSEADDR_ON;
import static com.github.onacit.__SocketUtils.SO_REUSEPORT_ON;

/**
 * A minimal TCP client that connects to a server and sends a random byte to it.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Rfc862Tcp1Client_Socket extends Rfc862Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = new Socket()) { // -> close() -> IOException
            // ------------------------------------------------------------------------------- SO_REUSEADDR/SO_REUSEPORT
            SO_REUSEADDR_ON(client); // IOException
            SO_REUSEPORT_ON(client); // IOException
            // ------------------------------------------------------------------------------------------------- connect
            assert !client.isConnected();
            final var endpoint = __Utils.parseSocketAddress(_Constants.PORT, args).orElse(_Constants.SERVER_ENDPOINT);
            log.debug("endpoint: {}", endpoint);
            client.connect(endpoint);
            assert client.isConnected();
            assert client.isBound(); // !!!
            log.debug("connected to {}, through {}", client.getRemoteSocketAddress(), client.getLocalSocketAddress());
            // --------------------------------------------------------- keep reading lines, send/receive, until `!quit'
            __Utils.readLinesUntil(System.in, l -> {
                if (l.equalsIgnoreCase(__Constants.QUIT)) {
                    try {
                        client.close();
                    } catch (final IOException ioe) {
                        log.error("failed to close " + client, ioe);
                    }
                    return true;
                }
                final var bytes = (l + System.lineSeparator()).getBytes();
                try {
                    client.getOutputStream().write(bytes); // IOException
                    client.getOutputStream().flush();
                    final int r = client.getInputStream().readNBytes(bytes, 0, bytes.length);
                    if (r < bytes.length) { // what does this mean?
                        return true;
                    }
                    System.out.print(new String(bytes));
                } catch (final IOException ioe) {
                    log.error("failed to send/receive", ioe);
                }
                return false;
            });
        }
    }
}
