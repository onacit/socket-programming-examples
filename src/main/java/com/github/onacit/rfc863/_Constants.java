package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
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
    static final boolean THROTTLE = true;

    // -----------------------------------------------------------------------------------------------------------------
    static final boolean TCP_CLIENT_BIND = true;

    static final boolean TCP_CLIENT_SHUTDOWN_INPUT = ThreadLocalRandom.current().nextBoolean();

    static final boolean TCP_SERVER_SHUTDOWN_OUTPUT = ThreadLocalRandom.current().nextBoolean();

    // -----------------------------------------------------------------------------------------------------------------
    static final boolean UDP_CLIENT_BIND = ThreadLocalRandom.current().nextBoolean();

    static final boolean UDP_CLIENT_CONNECT = ThreadLocalRandom.current().nextBoolean();

    // -----------------------------------------------------------------------------------------------------------------
    private _Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
