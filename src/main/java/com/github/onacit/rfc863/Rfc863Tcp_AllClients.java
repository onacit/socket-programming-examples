package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class Rfc863Tcp_AllClients {

    /**
     * An unmodifiable list of all subclasses extend {@link Rfc863Tcp$Client}.
     */
    static final List<Class<? extends Rfc863Tcp$Client>> CLASSES = List.of(
            Rfc863Tcp1Client_Socket.class,
            Rfc863Tcp2Client_SocketChannel_Blocking.class,
            Rfc863Tcp3Client_SocketChannel_NonBlocking.class,
            Rfc863Tcp4Client_AsynchronousSocketChannel_Future.class,
            Rfc863Tcp5Client_AsynchronousSocketChannel.class
    );

    public static void main(final String... args) {
        __Utils.startAll(CLASSES);
    }
}
