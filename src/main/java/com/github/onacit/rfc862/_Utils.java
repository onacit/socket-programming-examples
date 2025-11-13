package com.github.onacit.rfc862;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
final class _Utils {

    static int getRandomTcpClientBufferCapacity() {
        return ThreadLocalRandom.current().nextInt(_Constants.TCP_CLIENT_BUFFER_CAPACITY_MAX) + 1;
    }

    static int getRandomTcpServerBufferCapacity() {
        return ThreadLocalRandom.current().nextInt(_Constants.TCP_SERVER_BUFFER_CAPACITY_MAX) + 1;
    }

    static ByteBuffer newTcpClientBuffer() {
        return ByteBuffer.allocate(getRandomTcpClientBufferCapacity());
    }

    static ByteBuffer newTcpServerBuffer() {
        return ByteBuffer.allocate(getRandomTcpServerBufferCapacity());
    }

    // -----------------------------------------------------------------------------------------------------------------
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
