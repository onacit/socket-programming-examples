package com.github.onacit.z_calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Slf4j
class _OperatorTest {

    @Test
    void NEGATIVE_ZERO_isZero_() {
        assertThat(_Operator.NEGATIVE_ZERO).isZero();
    }

    @Test
    void POSITIVE_ZERO_isZero_() {
        assertThat(_Operator.POSITIVE_ZERO).isZero();
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static float randomOperand() {
        return _Test_Utils.randomFloat();
    }

    private static Stream<Float> zeroFloats() {
        return _Test_Utils.zeroFloats();
    }

    // -----------------------------------------------------------------------------------------------------------------
    @EnumSource(_Operator.class)
    @ParameterizedTest
    void operate_doesNotThrow_(final _Operator operator) {
        final var operand1 = randomOperand();
        final var operand2 = randomOperand();
        assertThatCode(() -> {
            operator.operate(operand1, operand2);
        }).doesNotThrowAnyException();
    }

    // ------------------------------------------------------------------------------------------------------------- ADD
    @DisplayName("ADD")
    @Nested
    class ADD_Test {

    }

    // ------------------------------------------------------------------------------------------------------------- SUB
    @Nested
    class SUB_Test {

    }

    // ------------------------------------------------------------------------------------------------------------- MUL
    @Nested
    class MUL_Test {

    }

    // ------------------------------------------------------------------------------------------------------------- DIV
    @Nested
    class DIV_Test {

        private static Stream<Float> zeroFloats_() {
            return zeroFloats();
        }

        @MethodSource({"zeroFloats_"})
        @ParameterizedTest
        void operate_Zero_Operand2IsZero(final float operand2) {
            // --------------------------------------------------------------------------------------------------- given
            log.debug("{}", operand2 == -.0f);
            log.debug("{}", operand2 == +.0f);
            final var operand1 = randomOperand();
            // ---------------------------------------------------------------------------------------------------- when
            final var result = _Operator.DIV.operate(operand1, operand2);
            // ---------------------------------------------------------------------------------------------------- then
            assertThat(result).isZero();
            log.debug("result: {} {}", result, Long.toBinaryString(Double.doubleToLongBits(result)));
        }
    }

    // ------------------------------------------------------------------------------------------------------------- MOD
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

    // ------------------------------------------------------------------------------------------------------------ SQRT
}