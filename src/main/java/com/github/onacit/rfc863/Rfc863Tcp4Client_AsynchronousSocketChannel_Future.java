package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp4Client_AsynchronousSocketChannel_Future extends Rfc863Tcp$Client {

    public static void main(final String... args) throws IOException, InterruptedException, ExecutionException {
        try (var client = AsynchronousSocketChannel.open()) { // IOException
            // ----------------------------------------------------------------------------------------- bind (optional)
            client.bind(new InetSocketAddress(__Constants.ANY_LOCAL, 0));
            // ------------------------------------------------------------------------------------------------- connect
            final var connecting = client.connect(_Constants.SERVER_ENDPOINT);
            final var result = connecting.get(); // InterruptedException, ExecutionException
            assert result == null;
            log.debug("connected to {}, through {}",
                      client.getRemoteAddress(), // IOException
                      client.getLocalAddress() // IOException
            );
            // --------------------------------------------------------------------- read 'quit', and close the <client>
            __Utils.readQuitAndClose(true, client);
            // ------------------------------------------------------------------------------- keep sending random bytes
            final var src = ByteBuffer.allocate(1);
            assert src.capacity() > 0;
            while (client.isOpen()) {
                __Utils.randomizeAvailableAndContent(src);
                final var writing = client.write(src);
                final var w = writing.get(); // InterruptedException, ExecutionException
                assert w >= 0;
                if (_Constants.THROTTLE) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
                }
            }
        }
    }
}
