package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public final class __IoConstants {

    // -----------------------------------------------------------------------------------------------------------------
//    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final Charset CHARSET = Charset.defaultCharset();

    // -----------------------------------------------------------------------------------------------------------------
    public static final String QUIT = "!quit";

    // -----------------------------------------------------------------------------------------------------------------
    private __IoConstants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
