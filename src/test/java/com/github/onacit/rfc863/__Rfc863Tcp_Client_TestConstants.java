package com.github.onacit.rfc863;

import java.util.List;

final class __Rfc863Tcp_Client_TestConstants {

    static final List<Class<? extends _Rfc863Tcp_Client>> CLIENT_CLASSES = List.of(
            Rfc863Tcp1Client.class,
            Rfc863Tcp2Client.class,
            Rfc863Tcp3Client.class,
            Rfc863Tcp4Client.class,
            Rfc863Tcp5Client.class
    );

    private __Rfc863Tcp_Client_TestConstants() {
        throw new AssertionError("instantiation is not allowed");
    }
}