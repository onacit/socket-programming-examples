package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
final class _Utils {

    static void logDiscarding(final int octet, final SocketAddress address) {
        log.debug("discarding, {}, received from {}", __Utils.formatOctet(octet), address);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private _Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
