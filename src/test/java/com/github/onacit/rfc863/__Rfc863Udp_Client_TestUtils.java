package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
final class __Rfc863Udp_Client_TestUtils {

    static Class<? extends Rfc863Udp$Client> randomClientClass() {
        assert !Rfc863Udp_AllClients.CLASSES.isEmpty();
        return Rfc863Udp_AllClients.CLASSES.get(
                ThreadLocalRandom.current().nextInt(Rfc863Udp_AllClients.CLASSES.size())
        );
    }

    private __Rfc863Udp_Client_TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}