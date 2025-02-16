package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public final class __Constants {

    // -----------------------------------------------------------------------------------------------------------------
    public static final InetAddress HOST;

    static {
        InetAddress host = null;
        for (final var h : new String[]{"::", "0.0.0.0"}) {
            try {
                host = InetAddress.getByName(h);
                break;
            } catch (final UnknownHostException uhe) {
                log.error("failed to get host address for '{}'", h, uhe);
            }
        }
        if (host == null) {
            throw new RuntimeException("failed to get host address");
        }
        HOST = host;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
