package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp2Client_SocketChannel_Blocking extends Rfc863Tcp$Client {

    /**
     * .
     *
     * @param args .
     * @throws IOException          .
     * @throws InterruptedException .
     * @see <a
     * href="https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/SocketChannel.html">java.nio.channels.SocketChannel</a>
     */
    public static void main(final String... args) throws IOException, InterruptedException {
        try (var client = SocketChannel.open()) { // IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            if (_Constants.TCP_CLIENT_BIND_MANUALLY) {
                client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0)); // IOException
                assert client.socket().isBound();
                log.debug("bound to {}", client.getLocalAddress());
            }
            // ------------------------------------------------------------------------------------------------- connect
            assert !client.isConnected();
            final var connected = client.connect(_Constants.SERVER_ENDPOINT); // IOException
            assert connected;
            assert client.isConnected();
            log.debug("connected to {}, through {}", client.getRemoteAddress(), client.getLocalAddress());
            assert client.socket().isBound();
            // ------------------------------------------------------------------------------- shutdown input (optional)
            if (_Constants.TCP_CLIENT_SHUTDOWN_INPUT) {
                client.shutdownInput(); // IOException
                {
                    final var r = client.read(ByteBuffer.allocate(0)); // IOException
                    assert r == -1 : "shouldn't read; as the input has been shut down";
                }
                {
                    final var r = client.read(ByteBuffer.allocate(1)); // IOException
                    assert r == -1 : "shouldn't read; as the input has been shut down";
                }
            }
            // --------------------------------------------------------------------- read 'quit', and close the <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------- keep sending random bytes
            assert client.isBlocking(); // !!!
            for (final var src = ByteBuffer.allocate(1); client.isOpen(); ) {
                assert src.capacity() > 0;
                {
                    __Utils.randomizeAvailableAndContent(src);
                    assert src.hasRemaining();
                }
                final var w = client.write(src); // IOException
                assert w >= 0;
                if (_Constants.TCP_CLIENT_THROTTLE) {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(1024L) + 1024L); // InterruptedException
                }
            }
        }
    }
}
