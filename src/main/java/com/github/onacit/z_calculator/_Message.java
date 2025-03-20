package com.github.onacit.z_calculator;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class _Message {

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERATOR = 0;

    static final int LENGTH_OPERATOR = Integer.BYTES;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERAND1 = OFFSET_OPERATOR + LENGTH_OPERATOR;

    static final int LENGTH_OPERAND1 = Float.BYTES;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERAND2 = OFFSET_OPERAND1 + LENGTH_OPERAND1;

    static final int LENGTH_OPERAND2 = Float.BYTES;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERAND = OFFSET_OPERAND1;

    static final int LENGTH_OPERAND = LENGTH_OPERAND1 + LENGTH_OPERAND2;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_RESULT = OFFSET_OPERAND + LENGTH_OPERAND;

    static final int LENGTH_RESULT = LENGTH_OPERAND;

    // -----------------------------------------------------------------------------------------------------------------
    static final int BYTES = LENGTH_OPERATOR + LENGTH_OPERAND + LENGTH_RESULT;

    // -------------------------------------------------------------------------------------------------------- operator
    public _Operator operator() {
        final var bytes = new byte[LENGTH_OPERATOR];
        operatorBuffer.get(0, bytes);
        final var name = new String(bytes, StandardCharsets.UTF_8).strip();
        try {
            return _Operator.valueOf(name); // IllegalArgumentException
        } catch (final IllegalArgumentException iae) {
            return null;
        }
    }

    public _Message operator(final _Operator operator) {
        Objects.requireNonNull(operator, "operator is null");
        final var name = operator.name();
        final var bytes = Arrays.copyOf(name.getBytes(StandardCharsets.UTF_8), LENGTH_OPERATOR);
        operatorBuffer.put(0, bytes);
        return this;
    }

    // -------------------------------------------------------------------------------------------------------- operand1
    public float operand1() {
        return operand1Buffer.get(0);
    }

    public _Message operand1(final float operand1) {
        operand1Buffer.put(0, operand1);
        return this;
    }

    // -------------------------------------------------------------------------------------------------------- operand2
    public float operand2() {
        return operand2Buffer.get(0);
    }

    public _Message operand2(final float operand2) {
        operand2Buffer.put(0, operand2);
        return this;
    }

    // --------------------------------------------------------------------------------------------------------- operand
    public double operand() {
        return operandBuffer.get(0);
    }

    public _Message operand(final double operand) {
        operandBuffer.put(0, operand);
        return this;
    }

    // ---------------------------------------------------------------------------------------------------------- result
    public double result() {
        return resultBuffer.get(0);
    }

    public _Message result(final double result) {
        resultBuffer.put(0, result);
        return this;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private final ByteBuffer buffer = ByteBuffer.allocate(BYTES);

    // -----------------------------------------------------------------------------------------------------------------
    private final ByteBuffer operatorBuffer = buffer.slice(OFFSET_OPERATOR, LENGTH_OPERATOR);

    private final FloatBuffer operand1Buffer = buffer.slice(OFFSET_OPERAND1, LENGTH_OPERAND1).asFloatBuffer();

    private final FloatBuffer operand2Buffer = buffer.slice(OFFSET_OPERAND2, LENGTH_OPERAND2).asFloatBuffer();

    private final DoubleBuffer operandBuffer = buffer.slice(OFFSET_OPERAND, LENGTH_OPERAND).asDoubleBuffer();

    private final DoubleBuffer resultBuffer = buffer.slice(OFFSET_RESULT, LENGTH_RESULT).asDoubleBuffer();
}
