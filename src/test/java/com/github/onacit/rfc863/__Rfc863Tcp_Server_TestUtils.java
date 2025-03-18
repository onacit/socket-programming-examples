package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
final class __Rfc863Tcp_Server_TestUtils {

    static Class<? extends Rfc863Tcp$Server> randomServerClass() {
        assert !__Rfc863Tcp_Server_TestConstants.SERVER_CLASSES.isEmpty();
        return __Rfc863Tcp_Server_TestConstants.SERVER_CLASSES.get(
                ThreadLocalRandom.current().nextInt(__Rfc863Tcp_Server_TestConstants.SERVER_CLASSES.size())
        );
    }

    private __Rfc863Tcp_Server_TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}