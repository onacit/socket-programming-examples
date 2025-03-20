package com.github.onacit.z_calculator;

enum _Operator {

    IDENTITY() {
        @Override
        double operate(final float operand1, final float operand2) {
            return _Operator.operand(operand1, operand2);
        }
    },

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
//                final var i1 = Float.floatToRawIntBits(operand1);
//                final var i2 = Float.floatToRawIntBits(operand2);
//                if (i1 < 0 && i2 < 0) {
//                    return
//                }
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

    // -----------------------------------------------------------------------------------------------------------------
    static final int POSITIVE_ZERO_BITS = 0b0_00000000_00000000000000000000000;

    static final int NEGATIVE_ZERO_BITS = 0b1_00000000_00000000000000000000000;

    static final float POSITIVE_ZERO = Float.intBitsToFloat(POSITIVE_ZERO_BITS);

    static final float NEGATIVE_ZERO = Float.intBitsToFloat(NEGATIVE_ZERO_BITS);

    // -----------------------------------------------------------------------------------------------------------------
    private static double operand(float operand1, float operand2) {
        return ((long) Float.floatToRawIntBits(operand1) << Integer.SIZE) | Float.floatToRawIntBits(operand2);
    }

    // -----------------------------------------------------------------------------------------------------------------
    _Operator() {
    }

    // -----------------------------------------------------------------------------------------------------------------
    abstract double operate(float operand1, float operand2);
}
