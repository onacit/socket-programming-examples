package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
final class __Rfc863Tcp_Server_TestUtils {

    static Class<? extends Rfc863Tcp$Server> randomServerClass() {
        assert !Rfc863Tcp_AllServers.CLASSES.isEmpty();
        return Rfc863Tcp_AllServers.CLASSES.get(
                ThreadLocalRandom.current().nextInt(Rfc863Tcp_AllServers.CLASSES.size())
        );
    }

    private __Rfc863Tcp_Server_TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}