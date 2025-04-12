package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * A class whose {@link Rfc863Tcp_AllClients#main(String...) main} method starts processes of all subclasses extend
 * {@link Rfc862Tcp$Client}.
 */
@Slf4j
class Rfc863Tcp_AllClients {

    /**
     * An unmodifiable list of all subclasses of {@link Rfc862Tcp$Client}.
     */
    static final List<Class<? extends Rfc862Tcp$Client>> CLASSES = List.of(
            Rfc863Tcp1Client_Socket.class,
            Rfc863Tcp2Client_SocketChannel_Blocking.class,
            Rfc863Tcp3Client_SocketChannel_NonBlocking.class,
            Rfc863Tcp4Client_AsynchronousSocketChannel_Future.class,
            Rfc863Tcp5Client_AsynchronousSocketChannel.class
    );

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Starts all processes of {@link #CLASSES}.
     *
     * @param args an array of command line arguments
     */
    public static void main(final String... args) {
        __Utils.startAll(CLASSES);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private Rfc863Tcp_AllClients() {
        throw new AssertionError("instantiation is not allowed");
    }
}
