package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class Rfc863Tcp_AllServers {

    /**
     * An unmodifiable list of all subclasses of {@link Rfc863Tcp$Server}.
     */
    static final List<Class<? extends Rfc863Tcp$Server>> CLASSES = List.of(
            Rfc863Tcp1Server_ServerSocket.class,
            Rfc863Tcp2Server_ServerSocketChannel_Blocking.class,
            Rfc863Tcp3Server_ServerSocketChannel_NonBlocking.class,
            Rfc863Tcp5Server_AsynchronousServerSocketChannel.class,
            Rfc863Tcp4Server_AsynchronousServerSocketChannel_Future.class
    );

    /**
     * Starts processes of {@link #CLASSES}.
     *
     * @param args an array of command line arguments.
     */
    public static void main(final String... args) {
        __Utils.startAll(CLASSES);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private Rfc863Tcp_AllServers() {
        throw new AssertionError("instantiation is not allowed");
    }
}
