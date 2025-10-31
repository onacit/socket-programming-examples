package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
final class __Rfc863Tcp_Client_TestUtils {

    static Class<? extends Rfc863Tcp$Client> randomClientClass() {
        assert !Rfc863Tcp_AllClients.CLASSES.isEmpty();
        return Rfc863Tcp_AllClients.CLASSES.get(
                ThreadLocalRandom.current().nextInt(Rfc863Tcp_AllClients.CLASSES.size())
        );
    }

    private __Rfc863Tcp_Client_TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}