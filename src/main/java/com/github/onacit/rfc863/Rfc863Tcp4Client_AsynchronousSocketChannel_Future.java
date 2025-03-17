package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class Rfc863Tcp4Client_AsynchronousSocketChannel_Future extends _Rfc863Tcp_Client {

    /**
     * .
     *
     * @param args .
     * @throws IOException          .
     * @throws InterruptedException .
     * @throws ExecutionException   .
     * @see AsynchronousSocketChannel#connect(SocketAddress)
     * @see AsynchronousSocketChannel#write(ByteBuffer)
     * @see Future#get()
     */
    public static void main(final String... args) throws IOException, InterruptedException, ExecutionException {
        try (var client = AsynchronousSocketChannel.open()) { // IOException

            // ------------------------------------------------------------------------------------------------- connect
            final var connecting = client.connect(_Constants.SERVER_ENDPOINT);
            final var result = connecting.get(); // InterruptedException, ExecutionException
            assert result == null;
            log.debug("connected to {}, through {}", client.getRemoteAddress(), client.getLocalAddress());

            // -------------------------------------------------------------------------- read-quit-and-close-the-client
            __Utils.readQuitAndClose(true, client);

            // ---------------------------------------------------------------------------------------------- send-bytes
            for (final var src = ByteBuffer.allocate(1); client.isOpen(); ) {
                ThreadLocalRandom.current().nextBytes(src.array());
                final var writing = client.write(src.clear()); // IOException
                final var w = writing.get(); // InterruptedException, ExecutionException
                assert w > 0;
                Thread.sleep(ThreadLocalRandom.current().nextInt(1024)); // InterruptedException
            }
        }
    }
}
