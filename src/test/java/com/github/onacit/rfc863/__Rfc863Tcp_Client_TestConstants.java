package com.github.onacit.rfc863;

import java.util.List;

final class __Rfc863Tcp_Client_TestConstants {

    static final List<Class<? extends _Rfc863Tcp_Client>> CLIENT_CLASSES = List.of(
            Rfc863Tcp1Client_Socket.class,
            Rfc863Tcp2Client_SocketChannel_Blocking.class,
            Rfc863Tcp3Client_SocketChannel_NonBlocking.class,
            Rfc863Tcp4Client_AsynchronousSocketChannel_Future.class,
            Rfc863Tcp5Client_AsynchronousSocketChannel.class
    );

    private __Rfc863Tcp_Client_TestConstants() {
        throw new AssertionError("instantiation is not allowed");
    }
}