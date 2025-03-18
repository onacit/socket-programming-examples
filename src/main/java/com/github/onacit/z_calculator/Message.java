package com.github.onacit.z_calculator;

import java.nio.ByteBuffer;
import java.util.Objects;

public class Message {

    static final int BYTES_OPERATOR = Integer.BYTES;

    static final int BYTES_OPERAND1 = Float.BYTES;

    static final int BYTES_OPERAND2 = Float.BYTES;

    static final int BYTES_OPERAND = BYTES_OPERAND1 + BYTES_OPERAND2;

    static final int BYTES_RESULT = BYTES_OPERAND;

    static final int BYTES = BYTES_OPERATOR + BYTES_OPERAND + BYTES_RESULT;

    // -----------------------------------------------------------------------------------------------------------------
    public enum Operator {

        ADD() {
            @Override
            double operate(final double operand1, final double operand2) {
                return 0;
            }
        },

        SUB() {
            @Override
            double operate(final double operand1, final double operand2) {
                return 0;
            }
        },

        MUL() {
            @Override
            double operate(final double operand1, final double operand2) {
                return 0;
            }
        },

        DIV() {
            @Override
            double operate(final double operand1, final double operand2) {
                return 0;
            }
        },

        MOD() {
            @Override
            double operate(final double operand1, final double operand2) {
                return 0;
            }
        },

        SQRT() {
            @Override
            double operate(final double operand1, final double operand2) {
                return 0;
            }
        };

        Operator() {
        }

        abstract double operate(double operand1, double operand2);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public void operator(final Operator operator) {
        Objects.requireNonNull(operator, "operator is null");
    }

    // -----------------------------------------------------------------------------------------------------------------
    private final ByteBuffer buffer = ByteBuffer.allocate(BYTES);
}
