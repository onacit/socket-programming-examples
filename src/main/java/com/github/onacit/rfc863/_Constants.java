package com.github.onacit.rfc863;

import com.github.onacit.__Constants;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

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
    static final boolean BIND_CLIENT_EXPLICITLY = true;

    static final boolean SHUTDOWN_INPUT_IN_CLIENT_SIDE = true;

    static final boolean SHUTDOWN_OUTPUT_IN_SERVER_SIDE = true;

    // -----------------------------------------------------------------------------------------------------------------
    private _Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
