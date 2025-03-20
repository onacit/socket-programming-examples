package com.github.onacit.z_calculator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

/**
 * A class for unit-testing {@link _Message} class.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
class _MessageTest {

    @Test
    void newInstance_operand1IsZero_() {
        // ------------------------------------------------------------------------------------------------------- given
        final var instance = new _Message();
        // -------------------------------------------------------------------------------------------------------- when
        final var operand1 = instance.getOperand1();
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(operand1).isZero();
    }

    @Test
    void newInstance_operand2IsZero_() {
        // ------------------------------------------------------------------------------------------------------- given
        final var instance = new _Message();
        // -------------------------------------------------------------------------------------------------------- when
        final var operand1 = instance.getOperand2();
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(operand1).isZero();
    }

    @Test
    void newInstance_operandIsZero_() {
        // ------------------------------------------------------------------------------------------------------- given
        final var instance = new _Message();
        // -------------------------------------------------------------------------------------------------------- when
        final var operand = instance.getOperand();
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(operand).isZero();
    }

    @Test
    void newInstance_resultIsZero_() {
        // ------------------------------------------------------------------------------------------------------- given
        final var instance = new _Message();
        // -------------------------------------------------------------------------------------------------------- when
        final var result = instance.getResult();
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(result).isZero();
    }

    @Test
    void toString_NotBlank_() {
        // ------------------------------------------------------------------------------------------------------- given
        final var instance = new _Message();
        // -------------------------------------------------------------------------------------------------------- when
        final var string = instance.toString();
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(string).isNotBlank();
    }

    // -------------------------------------------------------------------------------------------------------- operator
    @Nested
    class OperatorTest {

        @DisplayName("getOperator()Operator")
        @Nested
        class GetOperatorTest {

            @Test
            void __() {
                // ----------------------------------------------------------------------------------------------- given
                final var instance = new _Message();
                // ------------------------------------------------------------------------------------------------ when
                final var operator = instance.getOperator();
                // ------------------------------------------------------------------------------------------------ then
                assertThat(operator).isSameAs(_Operator.IDENTITY);
            }

            @EnumSource(_Operator.class)
            @ParameterizedTest
            void __(final _Operator operator) {
                // ----------------------------------------------------------------------------------------------- given
                final var instance = spy(new _Message());
                given(instance.getOperatorString())
                        .willReturn(operator.name());
                // ------------------------------------------------------------------------------------------------ when
                final var result = instance.getOperator();
                assertThat(result).isSameAs(operator);
            }
        }

        @DisplayName("setOperator(operator)V")
        @Nested
        class SetOperatorTest {

            @EnumSource(_Operator.class)
            @ParameterizedTest
            void __(final _Operator operator) {
                // ----------------------------------------------------------------------------------------------- given
                final var instance = spy(new _Message());
                // ------------------------------------------------------------------------------------------- when/then
                assertThatCode(() -> {
                    instance.setOperator(operator);
                }).doesNotThrowAnyException();
            }
        }

        @DisplayName("operator(operator)_Message")
        @Nested
        class _OperatorTest {

            @EnumSource(_Operator.class)
            @ParameterizedTest
            void __(final _Operator operator) {
                // ----------------------------------------------------------------------------------------------- given
                final var instance = spy(new _Message());
                // ------------------------------------------------------------------------------------------------ when
                final var result = instance.operator(operator);
                // ------------------------------------------------------------------------------------------------ then
                assertThat(result).isSameAs(instance);
                Mockito.verify(instance, times(1)).setOperator(operator);
            }
        }

        @DisplayName("operator(operator).getOperator()operator")
        @EnumSource(_Operator.class)
        @ParameterizedTest
        void __(final _Operator operator) {
            // --------------------------------------------------------------------------------------------------- given
            final var instance = new _Message();
            // ---------------------------------------------------------------------------------------------------- when
            final var result = instance.operator(operator).getOperator();
            // ---------------------------------------------------------------------------------------------------- then
            assertThat(result).isSameAs(operator);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
}