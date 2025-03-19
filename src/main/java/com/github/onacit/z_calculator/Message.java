package com.github.onacit.z_calculator;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class Message {

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERATOR = 0;

    static final int LENGTH_OPERATOR = Integer.BYTES;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERAND1 = OFFSET_OPERATOR + LENGTH_OPERATOR;

    static final int LENGTH_OPERAND1 = Float.BYTES;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERAND2 = OFFSET_OPERAND1 + LENGTH_OPERAND1

    static final int LENGTH_OPERAND2 = Float.BYTES;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERAND = OFFSET_OPERAND1;

    static final int LENGTH_OPERAND = LENGTH_OPERAND1 + LENGTH_OPERAND2;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_RESULT = OFFSET_OPERAND + LENGTH_OPERAND;

    static final int LENGTH_RESULT = LENGTH_OPERAND;

    // -----------------------------------------------------------------------------------------------------------------
    static final int BYTES = LENGTH_OPERATOR + LENGTH_OPERAND + LENGTH_RESULT;

    // -----------------------------------------------------------------------------------------------------------------
    public enum Operator {

        ADD() {
            @Override
            double operate(final float operand1, final float operand2) {
                return (double) operand1 + operand2;
            }
        },

        SUB() {
            @Override
            double operate(final float operand1, final float operand2) {
                return (double) operand1 - operand2;
            }
        },

        MUL() {
            @Override
            double operate(final float operand1, final float operand2) {
                return (double) operand1 * operand2;
            }
        },

        DIV() {
            @Override
            double operate(final float operand1, final float operand2) {
                if (operand2 == .0f) {
                    return .0d;
                }
                return (double) operand1 / operand2;
            }
        },

        MOD() {
            @Override
            double operate(final float operand1, final float operand2) {
                if (operand2 == .0f) {
                    return .0d;
                }
                return (double) operand1 % operand2;
            }
        },

        SQRT() {
            @Override
            double operate(final float operand1, final float operand2) {
                return Math.sqrt(operand(operand1, operand2));
            }
        };

        private static double operand(float operand1, float operand2) {
            return ((long) Float.floatToRawIntBits(operand1) << Integer.SIZE) | Float.floatToRawIntBits(operand2);
        }

        Operator() {
        }

        abstract double operate(float operand1, float operand2);
    }

    // -------------------------------------------------------------------------------------------------------- operator
    public Operator operator() {
        final var bytes = new byte[LENGTH_OPERATOR];
        buffer.get(OFFSET_OPERATOR, bytes);
        final var name = new String(bytes, StandardCharsets.UTF_8).strip();
        return Operator.valueOf(name); // IllegalArgumentException
    }

    public Message operator(final Operator operator) {
        Objects.requireNonNull(operator, "operator is null");
        final var name = operator.name();
        final var bytes = Arrays.copyOf(name.getBytes(StandardCharsets.UTF_8), LENGTH_OPERATOR);
        buffer.put(OFFSET_OPERATOR, bytes);
        return this;
    }

    // -------------------------------------------------------------------------------------------------------- operand1
    public float operand1() {
        return operand1Buffer.get();
    }

    public Message operand1(final float operand1) {
        operand1Buffer.put(operand1);
        return this;
    }

    // -------------------------------------------------------------------------------------------------------- operand2
    public float operand2() {
        return operand2Buffer.get();
    }

    public Message operand2(final float operand2) {
        operand2Buffer.put(operand2);
        return this;
    }

    // --------------------------------------------------------------------------------------------------------- operand
    public double operand() {
        return operandBuffer.get();
    }

    public Message operand(final double operand) {
        operandBuffer.put(operand);
        return this;
    }

    // ---------------------------------------------------------------------------------------------------------- result
    public double result() {
        return resultBuffer.get();
    }

    public Message result(final double result) {
        resultBuffer.put(result);
        return this;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private final ByteBuffer buffer = ByteBuffer.allocate(BYTES);

    private final FloatBuffer operand1Buffer = buffer.slice(OFFSET_OPERAND1, LENGTH_OPERAND1).asFloatBuffer();

    private final FloatBuffer operand2Buffer = buffer.slice(OFFSET_OPERAND2, LENGTH_OPERAND2).asFloatBuffer();

    private final DoubleBuffer operandBuffer = buffer.slice(OFFSET_OPERAND, LENGTH_OPERAND).asDoubleBuffer();

    private final DoubleBuffer resultBuffer = buffer.slice(OFFSET_RESULT, LENGTH_RESULT).asDoubleBuffer();
}
