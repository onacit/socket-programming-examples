package com.github.onacit.z_calculator;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

class _Message {

    static final Charset OPERATOR_CHARSET = StandardCharsets.UTF_8;

    // -----------------------------------------------------------------------------------------------------------------
    static final int OFFSET_OPERATOR = 0;

    static final int LENGTH_OPERATOR = Long.BYTES;

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

    static final int LENGTH_RESULT = Double.BYTES;

    // -----------------------------------------------------------------------------------------------------------------
    private static final int BYTES = OFFSET_RESULT + LENGTH_RESULT;

    // ---------------------------------------------------------------------------------------------------- CONSTRUCTORS
    _Message(final boolean direct) {
        super();
        buffer = direct ? ByteBuffer.allocateDirect(BYTES) : ByteBuffer.allocate(BYTES);
        operatorBuffer = buffer.slice(OFFSET_OPERATOR, LENGTH_OPERATOR);
        operand1Buffer = buffer.slice(OFFSET_OPERAND1, LENGTH_OPERAND1).asFloatBuffer();
        operand2Buffer = buffer.slice(OFFSET_OPERAND2, LENGTH_OPERAND2).asFloatBuffer();
        operandBuffer = buffer.slice(OFFSET_OPERAND, LENGTH_OPERAND).asDoubleBuffer();
        resultBuffer = buffer.slice(OFFSET_RESULT, LENGTH_RESULT).asDoubleBuffer();
        setOperator(_Operator.IDENTITY);
    }

    /**
     * Creates a new instance.
     */
    _Message() {
        this(false);
    }

    // ------------------------------------------------------------------------------------------------ java.lang.Object

    @Override
    public String toString() {
        return super.toString() + '{' +
               "operator=" + getOperatorString() +
               ",operand1=" + getOperand1() +
               ",operand2=" + getOperand2() +
               ",operand=" + getOperand() +
               ",result=" + getResult() +
               '}';
    }

    // -----------------------------------------------------------------------------------------------------------------
    public void readRequest(final InputStream in) throws IOException {
        Objects.requireNonNull(in, "in is null");
        configureBufferForRequest();
        final var len = buffer.remaining();
        if (buffer.hasArray()) {
            final var b = buffer.array();
            final var off = buffer.arrayOffset() + buffer.position();
            final var r = in.readNBytes(b, off, len);
            if (r < len) {
                throw new EOFException("end-of-file");
            }
            buffer.position(buffer.limit());
        } else {
            final var src = in.readNBytes(len);
            if (src.length < len) {
                throw new EOFException("end-of-file");
            }
            buffer.put(src);
        }
        assert !buffer.hasRemaining();
    }

    public void writeRequest(final OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out is null");
        configureBufferForRequest();
        final var len = buffer.remaining();
        if (buffer.hasArray()) {
            final var b = buffer.array();
            final var off = buffer.arrayOffset() + buffer.position();
            out.write(b, off, len);
            buffer.position(buffer.limit());
        } else {
            final var dst = new byte[len];
            buffer.get(dst);
            out.write(dst);
        }
        assert !buffer.hasRemaining();
    }

    public void read(final InputStream in) throws IOException {
        readRequest(in);
        configureBufferForResult();
        final int len = buffer.remaining();
        if (buffer.hasArray()) {
            final var b = buffer.array();
            final var off = buffer.arrayOffset() + buffer.position();
            final var r = in.readNBytes(b, off, len);
            if (r < len) {
                throw new EOFException("end-of-file");
            }
            buffer.position(buffer.limit());
        } else {
            final var src = in.readNBytes(len);
            if (src.length < len) {
                throw new EOFException("end-of-file");
            }
            buffer.put(src);
        }
        assert !buffer.hasRemaining();
    }

    public void write(final OutputStream out) throws IOException {
        writeRequest(out);
        configureBufferForResult();
        final int len = buffer.remaining();
        if (buffer.hasArray()) {
            final var b = buffer.array();
            final var off = buffer.arrayOffset() + buffer.position();
            out.write(b, off, len);
            buffer.position(buffer.limit());
        } else {
            final var dst = new byte[len];
            buffer.get(dst);
            out.write(dst);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public void readRequest(final ReadableByteChannel channel) throws IOException {
        Objects.requireNonNull(channel, "channel is null");
        configureBufferForRequest();
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) == -1) { // IOException
                throw new EOFException("end-of-file");
            }
        }
    }

    public void writeRequest(final WritableByteChannel channel) throws IOException {
        Objects.requireNonNull(channel, "channel is null");
        configureBufferForRequest();
        while (buffer.hasRemaining()) {
            channel.write(buffer); // IOException
        }
    }

    public void read(final ReadableByteChannel channel) throws IOException {
        readRequest(channel);
        configureBufferForRequest();
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) == -1) { // IOException
                throw new EOFException("end-of-file");
            }
        }
    }

    public void write(final WritableByteChannel channel) throws IOException {
        writeRequest(channel);
        configureBufferForResult();
        while (buffer.hasRemaining()) {
            channel.write(buffer); // IOException
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public <A> void readRequest(final AsynchronousByteChannel channel, final A attachment,
                                final CompletionHandler<Void, ? super A> handler)
            throws IOException {
        Objects.requireNonNull(channel, "channel is null");
        configureBufferForRequest();
        ;
        channel.read(buffer, attachment, new CompletionHandler<>() { // @formatter:off
            @Override public void completed(final Integer result, final A attachment) {
                if (result == -1) {
                    failed(new EOFException("end-of-file"), attachment);
                    return;
                }
                if (buffer.hasRemaining()) {
                    handler.completed(null, attachment);
                    return;
                }
                channel.read(buffer, null, this);
            }
            @Override public void failed(final Throwable exc, final A attachment) {
                handler.failed(exc, attachment);
            } // @formatter:on
        });
    }

    public <A> void writeRequest(final AsynchronousByteChannel channel, final A attachment,
                                 final CompletionHandler<Void, ? super A> handler)
            throws IOException {
        Objects.requireNonNull(channel, "channel is null");
        configureBufferForResult();
        channel.write(buffer, attachment, new CompletionHandler<>() { // @formatter:off
            @Override public void completed(final Integer result, final A attachment) {
                if (result == -1) {
                    failed(new EOFException("end-of-file"), attachment);
                    return;
                }
                if (buffer.hasRemaining()) {
                    handler.completed(null, attachment);
                    return;
                }
                channel.write(buffer, null, this);
            }
            @Override public void failed(final Throwable exc, final A attachment) {
                handler.failed(exc, attachment);
            } // @formatter:on
        });
    }

    public <A> void read(final AsynchronousByteChannel channel, final A attachment,
                         final CompletionHandler<Void, ? super A> handler)
            throws IOException {
        readRequest(channel, attachment, new CompletionHandler<>() { // @formatter:off
            @Override public void completed(final Void result, final A attachment) {
                buffer.limit(OFFSET_RESULT + LENGTH_RESULT);
                channel.read(buffer, attachment, new CompletionHandler<>() {
                    @Override public void completed(final Integer result, final A attachment) {
                        configureBufferForResult();
                        if (!buffer.hasRemaining()) {
                            handler.completed(null, attachment);
                            return;
                        }
                        channel.read(buffer, null, this);
                    }
                    @Override public void failed(final Throwable exc, final A attachment) {
                        handler.failed(exc, attachment);
                    }
                });
            }
            @Override public void failed(final Throwable exc, final A attachment) {
                handler.failed(exc, attachment);
            } // @formatter:on
        });
    }

    public <A> void write(final AsynchronousByteChannel channel, final A attachment,
                          final CompletionHandler<Void, ? super A> handler)
            throws IOException {
        writeRequest(channel, attachment, new CompletionHandler<>() { // @formatter:off
            @Override public void completed(final Void result, final A attachment) {
                buffer.limit(OFFSET_RESULT + LENGTH_RESULT);
                channel.write(buffer, attachment, new CompletionHandler<>() {
                    @Override public void completed(final Integer result, final A attachment) {
                        configureBufferForResult();
                        if (!buffer.hasRemaining()) {
                            handler.completed(null, attachment);
                            return;
                        }
                        channel.write(buffer, null, this);
                    }
                    @Override public void failed(final Throwable exc, final A attachment) {
                        handler.failed(exc, attachment);
                    }
                });
            }
            @Override public void failed(final Throwable exc, final A attachment) {
                handler.failed(exc, attachment);
            } // @formatter:on
        });
    }

    // -------------------------------------------------------------------------------------------------------- operator
    byte[] getOperatorBytes() {
        final var bytes = new byte[LENGTH_OPERATOR];
        operatorBuffer.get(0, bytes);
        return bytes;
    }

    void setOperatorBytes(final byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes is null");
        final var copy = Arrays.copyOf(bytes, LENGTH_OPERATOR);
        if (bytes.length < copy.length) {
            Arrays.fill(copy, bytes.length, copy.length, (byte) 0x20);
        }
        operatorBuffer.put(0, copy);
    }

    String getOperatorString() {
        return new String(getOperatorBytes()).strip();
    }

    void setOperatorString(final String operatorString) {
        Objects.requireNonNull(operatorString, "operatorString is null");
        setOperatorBytes(operatorString.getBytes(OPERATOR_CHARSET));
    }

    public _Operator getOperator() {
        return _Operator.valueOf(getOperatorString()); // IllegalArgumentException
    }

    public void setOperator(final _Operator operator) {
        Objects.requireNonNull(operator, "operator is null");
        setOperatorString(operator.name());
    }

    public _Message operator(final _Operator operator) {
        setOperator(operator);
        return this;
    }

    // -------------------------------------------------------------------------------------------------------- operand1
    public float getOperand1() {
        return operand1Buffer.get(0);
    }

    public void setOperand1(final float operand1) {
        operand1Buffer.put(0, operand1);
    }

    public _Message operand1(final float operand1) {
        setOperand1(operand1);
        return this;
    }

    // -------------------------------------------------------------------------------------------------------- operand2
    public float getOperand2() {
        return operand2Buffer.get(0);
    }

    public void setOperand2(final float operand2) {
        operand2Buffer.put(0, operand2);
    }

    public _Message operand2(final float operand2) {
        setOperand2(operand2);
        return this;
    }

    // --------------------------------------------------------------------------------------------------------- operand
    public double getOperand() {
        return operandBuffer.get(0);
    }

    public void setOperand(final double operand) {
        operandBuffer.put(0, operand);
    }

    public _Message operand(final double operand) {
        setOperand(operand);
        return this;
    }

    // ---------------------------------------------------------------------------------------------------------- result
    public double getResult() {
        return resultBuffer.get(0);
    }

    public void setResult(final double result) {
        resultBuffer.put(0, result);
    }

    public _Message result(final double result) {
        setResult(result);
        return this;
    }

    // ---------------------------------------------------------------------------------------------------------- buffer
    void configureBufferForRequest() {
        buffer.limit(OFFSET_RESULT).position(0);
    }

    void configureBufferForResult() {
        buffer.limit(OFFSET_RESULT + LENGTH_RESULT).position(OFFSET_RESULT);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private final ByteBuffer buffer;

    private final ByteBuffer operatorBuffer;

    private final FloatBuffer operand1Buffer;

    private final FloatBuffer operand2Buffer;

    private final DoubleBuffer operandBuffer;

    private final DoubleBuffer resultBuffer;
}
