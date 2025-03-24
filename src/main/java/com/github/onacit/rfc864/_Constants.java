package com.github.onacit.rfc864;

import com.github.onacit.__Constants;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

@Slf4j
final class _Constants {

    // -----------------------------------------------------------------------------------------------------------------
    static final int PORT = __Rfc864_Constants.PORT + 20000;

    // -----------------------------------------------------------------------------------------------------------------
    static final SocketAddress SERVER_ENDPOINT_TO_BIND = new InetSocketAddress(__Constants.ANY_LOCAL, PORT);

    static final SocketAddress SERVER_ENDPOINT;

    static {
        try {
            SERVER_ENDPOINT = new InetSocketAddress(InetAddress.getByName("::1"), PORT);
//            SERVER_ENDPOINT = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
        } catch (final UnknownHostException uhe) {
            throw new RuntimeException("failed to get the address of the local host", uhe);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    private _Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
