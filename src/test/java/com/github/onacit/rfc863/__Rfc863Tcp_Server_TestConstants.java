package com.github.onacit.rfc863;

import java.util.List;

final class __Rfc863Tcp_Server_TestConstants {

    static final List<Class<? extends _Rfc863Tcp_Server>> SERVER_CLASSES = List.of(
            Rfc863Tcp1Server_ServerSocket.class,
            Rfc863Tcp2Server_ServerSocketChannel_Blocking.class,
            Rfc863Tcp3Server_ServerSocketChannel_NonBlocking.class,
            Rfc863Tcp4Server_AsynchronousServerSocketChannel_Future.class,
            Rfc863Tcp5Server_AsynchronousServerSocketChannel.class
    );

    private __Rfc863Tcp_Server_TestConstants() {
        throw new AssertionError("instantiation is not allowed");
    }
}