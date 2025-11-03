package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
final class _Constants {

    // -----------------------------------------------------------------------------------------------------------------
    static final int PORT = __RFC863_Constants.PORT + 20000;

    // -----------------------------------------------------------------------------------------------------------------
    static final SocketAddress SERVER_ENDPOINT_TO_BIND = new InetSocketAddress(__Constants.ANY_LOCAL, PORT);

    static final SocketAddress SERVER_ENDPOINT;

    static {
        try {
            SERVER_ENDPOINT = new InetSocketAddress(InetAddress.getByName("::1"), PORT);
//            SERVER_ENDPOINT = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
        } catch (final UnknownHostException uhe) {
            throw new ExceptionInInitializerError("failed to get the address of the local host; " + uhe.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    static final boolean TCP_CLIENT_BIND_MANUALLY = false;

    static final int TCP_CLIENT_CONNECT_TIMEOUT = 0;

    static final boolean TCP_CLIENT_SHUTDOWN_INPUT = false;

    static final boolean TCP_CLIENT_THROTTLE = true;

    // -----------------------------------------------------------------------------------------------------------------
    static final int TCP_SERVER_BACKLOG = 50;

    static final boolean TCP_SERVER_SHUTDOWN_CLIENT_OUTPUT = false;

    // -----------------------------------------------------------------------------------------------------------------
    static final boolean UDP_CLIENT_BIND = ThreadLocalRandom.current().nextBoolean();

    static final boolean UDP_CLIENT_CONNECT = ThreadLocalRandom.current().nextBoolean();

    // -----------------------------------------------------------------------------------------------------------------
    private _Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
