package com.github.onacit.rfc864;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
final class _Utils {

    private static final ByteBuffer BUFFER = ByteBuffer.wrap(
            __Rfc864_Constants.PATTERN.getBytes(StandardCharsets.US_ASCII)
    ).asReadOnlyBuffer();

    static _Generator newPatternGenerator() {
        final var buffer = BUFFER.slice();
        final var indices = new int[]{0, 1};
        final var dst = ByteBuffer.allocate(3).flip();
        return () -> {
            dst.compact();
            if (indices[0] == 72) {
                if (dst.remaining() < 2) {
                    return buffer.flip();
                }
                dst.put((byte) 0x0D).put((byte) 0x0A); // CR LF
                indices[0] = 0;
                buffer.position(indices[1]++);
                if (indices[1] == buffer.capacity()) {
                    indices[1] = 0;
                }
            }
            while (dst.hasRemaining()) {
                if (!buffer.hasRemaining()) {
                    buffer.position(0);
                }
                dst.put(buffer.get());
                if (++indices[0] == 72) {
                    break;
                }
            }
            return dst.flip();
        };
    }

    // -----------------------------------------------------------------------------------------------------------------
    private _Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
