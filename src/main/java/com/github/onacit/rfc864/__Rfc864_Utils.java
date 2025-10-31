package com.github.onacit.rfc864;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class __Rfc864_Utils {

    public static byte[] newPatternArray() {
        return __Rfc864_Constants.PATTERN.getBytes(StandardCharsets.US_ASCII);
    }

    public static ByteBuffer newPatternBuffer() {
        return ByteBuffer.wrap(newPatternArray());
    }

    public static ByteBuffer newReadOnlyPatternBuffer() {
        return newPatternBuffer().asReadOnlyBuffer();
    }

    private __Rfc864_Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
