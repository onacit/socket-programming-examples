package com.github.onacit.rfc864;

public final class __Rfc864_Constants {

    public static final int PORT = 19;

    /**
     * A well-known pattern defined in <a href="https://datatracker.ietf.org/doc/html/rfc864">RFC-864 Character
     * Generator Protocol</a>. The value is {@value}.
     */
    public static final String PATTERN =
            "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ ";

    public static final int UDP_SERVER_CHARACTERS_MAX = 512;

    // -----------------------------------------------------------------------------------------------------------------
    private __Rfc864_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
