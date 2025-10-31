package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
final class __Rfc863Udp_Server_TestUtils {

    static Class<? extends Rfc863Udp$Server> randomServerClass() {
        assert !Rfc863Udp_AllServers.CLASSES.isEmpty();
        return Rfc863Udp_AllServers.CLASSES.get(
                ThreadLocalRandom.current().nextInt(Rfc863Udp_AllServers.CLASSES.size())
        );
    }

    private __Rfc863Udp_Server_TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}