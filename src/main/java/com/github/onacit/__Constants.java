package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

@Slf4j
public final class __Constants {

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The unspecified address
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/net/ipv6_guide/#special">Special IPv6
     * Address Types</a>
     */
    public static final InetAddress ANY_LOCAL;

    static {
        InetAddress anyLocal = null;
        for (final var host : new String[]{"::", "0.0.0.0"}) {
            try {
                anyLocal = InetAddress.getByName(host);
                break;
            } catch (final UnknownHostException uhe) {
                log.error("failed to get any local address for '{}'", host, uhe);
            }
        }
        if (anyLocal == null) {
            throw new RuntimeException("failed to get any-local address");
        }
        ANY_LOCAL = anyLocal;
        log.debug("any local: {}", ANY_LOCAL);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static final int UDP_PAYLOAD_MAX = 65527;

    // -----------------------------------------------------------------------------------------------------------------
//    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final Charset CHARSET = Charset.defaultCharset();

    // -----------------------------------------------------------------------------------------------------------------
    public static final String QUIT = "!quit";

    // -----------------------------------------------------------------------------------------------------------------
    private __Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
