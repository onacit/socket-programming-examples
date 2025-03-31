package com.github.onacit.z_calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.onacit.z_calculator._Test_Utils.randomFloat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Slf4j
class _FloatTest {

    private static Stream<Float> zeroFloats_() {
        return _Test_Utils.zeroFloats();
    }

    // -----------------------------------------------------------------------------------------------------------------
    @DisplayName("non-zero / zero = Infinite")
    @MethodSource({"zeroFloats_"})
    @ParameterizedTest
    void _Infinity_nonZeroDivideByZero(final float zero) {
        // ------------------------------------------------------------------------------------------------------- given
        final var a = randomFloat();
        assumeThat(a).isNotZero();
        // -------------------------------------------------------------------------------------------------------- when
        float r = a / zero;
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(r).isInfinite();
    }

    @DisplayName("zero / zero = NaN")
    @MethodSource({"zeroFloats_"})
    @ParameterizedTest
    void _NaN_zeroDivideByZero(final float zero) {
        // ------------------------------------------------------------------------------------------------------- given
        // -------------------------------------------------------------------------------------------------------- when
        float r = zero / zero;
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(r).isNaN();
    }

    @DisplayName("zero / non-zero = zero")
    @MethodSource({"zeroFloats_"})
    @ParameterizedTest
    void _NaN_zeroDivideByNonZero(final float zero) {
        // ------------------------------------------------------------------------------------------------------- given
        final float b = randomFloat();
        assumeThat(b).isNotZero();
        // -------------------------------------------------------------------------------------------------------- when
        float r = zero / b;
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(r).isZero();
    }

    @Disabled("may be zero")
    @DisplayName("non-zero / non-zero != zero")
    @Test
    void _NaN_nonZeroDivideByNonZero() {
        // ------------------------------------------------------------------------------------------------------- given
        final float a = randomFloat();
        assumeThat(a).isNotZero();
        final float b = randomFloat();
        assumeThat(b).isNotZero();
        // -------------------------------------------------------------------------------------------------------- when
        float r = a / b;
        log.debug("a: {}, b: {}, r: {}", a, b, r);
        // -------------------------------------------------------------------------------------------------------- then
//        assertThat(r).isNotZero(); // may be
    }
}
