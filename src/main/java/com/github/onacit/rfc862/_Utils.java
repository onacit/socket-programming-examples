package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

@Slf4j
final class _Utils {

    static void logEchoing(final int octet, final SocketAddress address) {
        log.debug("echoing, {}, back to {}", __Utils.formatOctet(octet), address);
    }

    static void logEchoed(final int octet, final SocketAddress address) {
        log.debug("echoed, {}, back from {}", __Utils.formatOctet(octet), address);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private _Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
