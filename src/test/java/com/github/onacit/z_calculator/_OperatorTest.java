package com.github.onacit.z_calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Slf4j
class _OperatorTest {

    private static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    @Nested
    class FloatTest {

        @ValueSource(ints = {
                _Operator.NEGATIVE_ZERO_BITS,
                _Operator.POSITIVE_ZERO_BITS
        })
        @ParameterizedTest
        void _Infinity_divideByZero(final int bits) {
            final float f = Float.intBitsToFloat(bits);
            float r = randomFloat() / f;
            assertThat(r).isInfinite();
        }

        @ValueSource(ints = {
                _Operator.NEGATIVE_ZERO_BITS,
                _Operator.POSITIVE_ZERO_BITS
        })
        @ParameterizedTest
        void _NaN_zeroDivideByZero(final int bits) {
            final float f = Float.intBitsToFloat(bits);
            float r = f / f;
            assertThat(r).isNaN();
        }
    }

    private static Stream<Float> operandStream() {
        return Stream.of(
                ThreadLocalRandom.current().nextFloat(),
                ThreadLocalRandom.current().nextFloat()
        );
    }

    private static Stream<Float> zeroFloats() {
        return Stream.of(
                        0b0_00000000_00000000000000000000000,
                        0b1_00000000_00000000000000000000000
                )
                .map(Float::intBitsToFloat);
    }

    @EnumSource(_Operator.class)
    @ParameterizedTest
    void operate_doesNotThrow_(final _Operator operator) {
        final var operand1 = ThreadLocalRandom.current().nextFloat();
        final var operand2 = ThreadLocalRandom.current().nextFloat();
        assertThatCode(() -> {
            operator.operate(operand1, operand2);
        }).doesNotThrowAnyException();
    }

    @Nested
    class ADD_Test {

    }

    @Nested
    class SUB_Test {

    }

    @Nested
    class MUL_Test {

    }

    @Nested
    class DIV_Test {

        private static Stream<Float> zeroFloats_() {
            return zeroFloats();
        }

        @MethodSource({"zeroFloats_"})
        @ParameterizedTest
        void operate_Zero_Operand2IsZero(final float operand2) {
            log.debug("{}", operand2 == -.0f);
            log.debug("{}", operand2 == +.0f);
            final var operand1 = ThreadLocalRandom.current().nextFloat();
            final var result = _Operator.DIV.operate(operand1, operand2);
            assertThat(result).isZero();
            log.debug("result: {} {}", result, Long.toBinaryString(Double.doubleToLongBits(result)));
        }
    }

    @Nested
    class MOD_Test {

        private static Stream<Float> zeroFloats_() {
            return zeroFloats();
        }

        @MethodSource({"zeroFloats_"})
        @ParameterizedTest
        void operate_Zero_Operand2IsZero(final float operand2) {
            final var operand1 = ThreadLocalRandom.current().nextFloat();
            final var result = _Operator.MOD.operate(operand1, operand2);
            assertThat(result).isZero();
        }
    }
}