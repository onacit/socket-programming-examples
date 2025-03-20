package com.github.onacit.z_calculator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class _Test_Utils {

    static float randomFloat() {
        return Float.intBitsToFloat(ThreadLocalRandom.current().nextInt());
    }

    static IntStream zeroIntBits() {
        return IntStream.of(
                _Operator.NEGATIVE_ZERO_BITS,
                _Operator.POSITIVE_ZERO_BITS
        );
    }

    static Stream<Float> zeroFloats() {
        return zeroIntBits()
                .mapToObj(Float::intBitsToFloat);
    }

    private _Test_Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
