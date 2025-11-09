package com.github.onacit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
public final class __Utils {

    /**
     * Starts a new thread keeps reading lines from specified reader until it reads a line tests with specified
     * predicate.
     *
     * @param daemon    a flag for the daemon status of the thread.
     * @param reader    the reader from which the thread reads lines.
     * @param predicate the predicate tests each line read from the {@code reader}.
     */
    public static void readAndTest(final boolean daemon, final BufferedReader reader,
                                   final Predicate<? super String> predicate) {
        Objects.requireNonNull(reader, "reader is null");
        Objects.requireNonNull(predicate, "predicate is null");
        Thread.ofPlatform().daemon(daemon).start(() -> {
            try {
                for (String l; (l = reader.readLine()) != null; ) {
                    if (predicate.test(l)) {
                        break;
                    }
                }
            } catch (final IOException ioe) {
                log.error("failed to read line", ioe);
            }
        });
    }

    public static void readAndTest(final boolean daemon, final Reader reader,
                                   final Predicate<? super String> predicate) {
        Objects.requireNonNull(reader, "reader is null");
        Objects.requireNonNull(predicate, "predicate is null");
        readAndTest(daemon, new BufferedReader(reader), predicate);
    }

    public static void readAndTest(final boolean daemon, final InputStream stream,
                                   final Predicate<? super String> predicate) {
        Objects.requireNonNull(stream, "stream is null");
        Objects.requireNonNull(predicate, "predicate is null");
        readAndTest(daemon, new InputStreamReader(stream), predicate);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void readAndAcceptOtherThanQuit(final boolean daemon, final BufferedReader reader,
                                                  final Consumer<? super String> consumer) {
        Objects.requireNonNull(reader, "reader is null");
        Objects.requireNonNull(consumer, "consumer is null");
        readAndTest(
                daemon,
                reader,
                l -> {
                    if (l.equalsIgnoreCase(__Constants.QUIT)) {
                        return true;
                    }
                    consumer.accept(l);
                    return false;
                }
        );
    }

    public static void readAndAcceptOtherThanQuit(final boolean daemon, final Reader reader,
                                                  final Consumer<? super String> consumer) {
        Objects.requireNonNull(reader, "reader is null");
        Objects.requireNonNull(consumer, "consumer is null");
        readAndAcceptOtherThanQuit(
                daemon,
                new BufferedReader(reader),
                consumer
        );
    }

    public static void readAndAcceptOtherThanQuit(final boolean daemon, final InputStream stream,
                                                  final Consumer<? super String> consumer) {
        Objects.requireNonNull(stream, "stream is null");
        Objects.requireNonNull(consumer, "consumer is null");
        readAndAcceptOtherThanQuit(
                daemon,
                new InputStreamReader(stream),
                consumer
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void readLinesUntil(final BufferedReader reader, final Predicate<? super String> predicate)
            throws IOException {
        Objects.requireNonNull(reader, "reader is null");
        for (String l; (l = reader.readLine()) != null; ) {
            if (predicate.test(l)) {
                break;
            }
        }
    }

    public static void readLinesUntil(final Reader reader, final Predicate<? super String> predicate)
            throws IOException {
        Objects.requireNonNull(reader, "reader is null");
        readLinesUntil(new BufferedReader(reader), predicate);
    }

    public static void readLinesUntil(final InputStream stream, final Predicate<? super String> predicate)
            throws IOException {
        Objects.requireNonNull(stream, "stream is null");
        readLinesUntil(new InputStreamReader(stream), predicate);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void readLinesWhile(final BufferedReader reader, final Predicate<? super String> predicate)
            throws IOException {
        readLinesUntil(
                reader,
                predicate.negate()
        );
    }

    public static void readLinesWhile(final Reader reader, final Predicate<? super String> predicate)
            throws IOException {
        Objects.requireNonNull(reader, "reader is null");
        readLinesWhile(
                new BufferedReader(reader),
                predicate
        );
    }

    public static void readLinesWhile(final InputStream stream, final Predicate<? super String> predicate)
            throws IOException {
        Objects.requireNonNull(stream, "stream is null");
        readLinesWhile(
                new InputStreamReader(stream),
                predicate
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    static void readQuit(final BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader, "reader is null");
        __Utils.readLinesUntil(reader, l -> l.toUpperCase().equalsIgnoreCase(__Constants.QUIT));
    }

    static void readQuit(final Reader reader) throws IOException {
        Objects.requireNonNull(reader, "reader is null");
        readQuit(new BufferedReader(reader));
    }

    static void readQuit(final InputStream stream) throws IOException {
        Objects.requireNonNull(stream, "stream is null");
        readQuit(new InputStreamReader(stream));
    }

    /**
     * Keep reading lines from the {@link System#in standard input stream} until it reads a line contains
     * {@value __Constants#QUIT}.
     *
     * @throws IOException if an I/O error occurs.
     */
    static void readQuit() throws IOException {
        readQuit(System.in);
    }

    /**
     * Starts a new thread which keeps reading lines from the {@link System#in standard input stream} until it reads a
     * line contains (case-insensitively) {@value __Constants#QUIT}, and then {@link Callable#call() calls} specified
     * task.
     *
     * @param daemon   a flag for starting the thread as s daemon; {@code true} for {@code daemon}; {@code false}
     *                 otherwise.
     * @param callable the task to {@link Callable#call() call}.
     */
    public static void readQuitAndCall(final boolean daemon, final Callable<?> callable) {
        Objects.requireNonNull(callable, "callable is null");
        Thread.ofPlatform().name("read-quit-and-call").daemon(daemon).start(() -> {
            try {
                readQuit();
            } catch (final IOException ioe) {
                log.error("failed to read 'quit'", ioe);
            } finally {
                try {
                    callable.call();
                } catch (final Exception e) {
                    log.debug("failed to call {}", callable, e);
                }
            }
        });
    }

    /**
     * Starts a new thread which keeps reading lines from the {@link System#in standard input stream} until it reads a
     * line contains (case-insensitively) {@value __Constants#QUIT}, and then {@link Runnable#run() runs} specified
     * task.
     *
     * @param daemon   a flag for starting the thread as s daemon; {@code true} for {@code daemon}; {@code false}
     *                 otherwise.
     * @param runnable the task to {@link Runnable#run() run}.
     * @see #readQuitAndCall(boolean, Callable)
     */
    public static void readQuitAndRun(final boolean daemon, final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        readQuitAndCall(
                daemon,
                () -> {
                    runnable.run();
                    return null;
                }
        );
    }

    /**
     * Starts a new thread which keeps reading lines from the {@link System#in standard input stream} until it reads a
     * line contains (case-insensitively) {@value __Constants#QUIT}, and then {@link Closeable#close() closes} specified
     * closeable.
     *
     * @param daemon    a flag for starting the thread as s daemon; {@code true} for {@code daemon}; {@code false}
     *                  otherwise.
     * @param closeable the closeable to {@link Closeable#close() close}.
     * @see #readQuitAndCall(boolean, Callable)
     * @see Closeable#close()
     */
    public static void readQuitAndClose(final boolean daemon, final Closeable closeable) {
        Objects.requireNonNull(closeable, "closeable is null");
        readQuitAndCall(
                daemon, // <daemon>
                () -> { // <callable>
                    closeable.close(); // IOException
                    log.debug("closed: {}", closeable);
                    return null;
                }
        );
    }

    public static void readQuitAndCountDown(final boolean daemon, final CountDownLatch latch) {
        Objects.requireNonNull(latch, "latch is null");
        readQuitAndRun(
                daemon,
                latch::countDown
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void readQuitAndCall(final boolean daemon, final Callable<?> callable,
                                       final Consumer<? super String> consumer) {
        Objects.requireNonNull(callable, "callable is null");
        Objects.requireNonNull(consumer, "consumer is null");
        Thread.ofPlatform().name("read-quit-and-call").daemon(daemon).start(() -> {
            final var reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
            try {
                for (String l; (l = reader.readLine()) != null; ) {
                    if (l.equalsIgnoreCase(__Constants.QUIT)) {
                        break;
                    }
                    consumer.accept(l);
                }
            } catch (final IOException ioe) {
                log.error("failed to read line", ioe);
            } finally {
                try {
                    callable.call();
                } catch (final Exception e) {
                    log.debug("failed to call {}", callable, e);
                }
            }
        });
    }

    public static void readQuitAndShutdown(final boolean daemon, final AsynchronousChannelGroup group,
                                           final Consumer<? super String> consumer) {
        Objects.requireNonNull(group, "group is null");
        readQuitAndCall(
                daemon,
                () -> {
                    group.shutdown();
                    return null;
                },
                consumer
        );
    }

    public static void readQuitAndShutdownNow(final boolean daemon, final AsynchronousChannelGroup group,
                                              final Consumer<? super String> consumer) {
        Objects.requireNonNull(group, "group is null");
        readQuitAndCall(
                daemon,
                () -> {
                    group.shutdownNow();
                    return null;
                },
                consumer
        );
    }

    public static void readQuitAndRun(final boolean daemon, final Runnable runnable,
                                      final Consumer<? super String> consumer) {
        Objects.requireNonNull(runnable, "runnable is null");
        readQuitAndCall(
                daemon,
                () -> {
                    runnable.run();
                    return null;
                },
                consumer
        );
    }

    public static void readQuitAndClose(final boolean daemon, final Closeable closeable,
                                        final Consumer<? super String> consumer) {
        Objects.requireNonNull(closeable, "closeable is null");
        readQuitAndCall(
                daemon,
                () -> {
                    closeable.close();
                    return null;
                },
                consumer
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    // https://stackoverflow.com/a/54790608/330457
    public static <R> R applyCommandAndClasspath(
            final BiFunction<? super String, ? super String, ? extends R> function) {
        Objects.requireNonNull(function, "function is null");
        final var info = ProcessHandle.current().info();
        final var command = info.command().orElseThrow();
        final String classpath;
        {
            String cp = null;
            final var arguments = info.arguments().orElseGet(() -> new String[0]);
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].equals("-classpath")) {
                    assert arguments.length >= i + 1;
                    cp = arguments[i + 1];
                    break;
                }
            }
            classpath = Optional.ofNullable(cp).orElse(".");
        }
        return function.apply(command, classpath);
    }

    public static void acceptCommandAndClasspath(final BiConsumer<? super String, ? super String> consumer) {
        Objects.requireNonNull(consumer, "consumer is null");
        applyCommandAndClasspath((cmd, cp) -> {
            consumer.accept(cmd, cp);
            return null;
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Randomizes content of specified buffer, between its {@code psotion} and {@code limit}.
     *
     * @param buffer the buffer.
     * @param <T>    buffer type parameter
     * @return given {@code buffer}.
     */
    public static <T extends ByteBuffer> T randomizeContent(final T buffer) {
        Objects.requireNonNull(buffer, "buffer is null");
        final var src = new byte[buffer.remaining()];
        ThreadLocalRandom.current().nextBytes(src);
        if (buffer.hasArray()) {
            System.arraycopy(src, 0, buffer.array(), buffer.position(), buffer.remaining());
        } else {
            buffer.put(buffer.position(), src);
        }
        return buffer;
    }

    /**
     * Randomizes specified buffer's {@code position} and {@code limit}.
     *
     * @param buffer the buffer.
     * @param <T>    buffer type parameter
     * @return given {@code buffer}.
     */
    public static <T extends ByteBuffer> T randomizeRemaining(final T buffer) {
        if (Objects.requireNonNull(buffer, "buffer is null").capacity() == 0) {
            throw new IllegalArgumentException("zero-capacity buffer: " + buffer);
        }
        buffer.limit(ThreadLocalRandom.current().nextInt(1, buffer.capacity() + 1));
        assert buffer.limit() > 0;
        buffer.position(ThreadLocalRandom.current().nextInt(buffer.limit()));
        assert buffer.position() < buffer.limit();
        assert buffer.hasRemaining();
        return buffer;
    }

    /**
     * Randomizes specified buffer's {@code position}, {@code limit}, and its content between the new available range.
     *
     * @param buffer the buffer.
     * @param <T>    buffer type parameter
     * @return given {@code buffer}.
     */
    public static <T extends ByteBuffer> T randomizeAvailableAndContent(final T buffer) {
        Objects.requireNonNull(buffer, "buffer is null");
        return randomizeContent(randomizeRemaining(buffer));
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static Process startProcess(final String... command) {
        Objects.requireNonNull(command, "command is null");
        final var builder = new ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            final var process = builder.start();
            log.debug("process: {}", process.info());
            return process;
        } catch (final IOException ioe) {
            log.error("failed to start " + builder.command(), ioe);
            return null;
        }
    }

    public static Process startProcess(final Class<?> mainClass) {
        return applyCommandAndClasspath(
                (cmd, cp) -> startProcess(new String[]{cmd, "-cp", cp, mainClass.getName()})
        );
    }

    public static void quitProcess(final Process process) {
        Objects.requireNonNull(process, "process is null");
        try {
            process.getOutputStream().write("quit\r\n".getBytes());
            process.getOutputStream().flush();
        } catch (final IOException ioe) {
            log.error("failed to write 'quit' to {}", process, ioe);
            log.error("destroying, forcibly, {}", process);
            process.destroyForcibly();
        }
    }

    public static void waitForProcess(final Process process) {
        Objects.requireNonNull(process, "process is null");
        while (process.isAlive()) {
            try {
                final var exited = process.waitFor(1L, TimeUnit.SECONDS);
                if (exited) {
                    break;
                }
            } catch (final InterruptedException ie) {
                log.error("interrupted while waiting for {}", process, ie);
                process.destroyForcibly();
            }
        }
    }

    public static void startAll(final List<? extends Class<?>> CLASSES) {
        final var latch = new CountDownLatch(CLASSES.size());
        // ----------------------------------------------------------------------------------------- start all processes
        final var processes = applyCommandAndClasspath((cmd, cp) -> CLASSES.stream()
                .map(c -> new String[]{cmd, "-cp", cp, c.getName()})
                .map(__Utils::startProcess)
                .peek(p -> {
                    if (p == null) {
                        latch.countDown();
                    }
                })
                .filter(Objects::nonNull)
                .peek(p -> {
                    log.debug("process: {}", p.info());
                    p.onExit().thenRun(() -> {
                        latch.countDown();
                        if (latch.getCount() == 0L) {
                            System.exit(0);
                        }
                    });
                })
                .toList()
        );
        // ------------------------------------------------------------------- read 'quit', write 'quit' to each process
        readQuitAndRun(false, () -> processes.forEach(__Utils::quitProcess));
        // ----------------------------------------------------------------------------------- wait all processes exited
        for (final var process : processes) {
            waitForProcess(process);
            assert !process.isAlive();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static Optional<SocketAddress> parseSocketAddress(final int defaultPort, final String... args) {
        final var options = new Options();
        options.addOption("h", "host address", false, "host address to bind/connect on/to");
        options.addOption("p", "port number", false, "port number to bind/connect on/to");
        final var parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException pe) {
            return Optional.empty();
        }
        final var host = Optional.ofNullable(cmd.getOptionValue('h'))
                .map(h -> {
                    try {
                        return InetAddress.getByName(h);
                    } catch (final UnknownHostException uhe) {
                        log.error("failed to get inet address by name: " + h, uhe);
                        return null;
                    }
                })
                .orElse(__Constants.ANY_LOCAL);
        final var port = Optional.ofNullable(cmd.getOptionValue('p'))
                .map(p -> {
                    try {
                        return Integer.parseInt(p);
                    } catch (final NumberFormatException nfe) {
                        return null;
                    }
                })
                .filter(p -> p >= 0 && p <= 65535)
                .orElse(defaultPort);
        return Optional.of(new InetSocketAddress(host, port));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // TODO: ScopedValue!!!
    private static final ThreadLocal<Formatter> OCTET_FORMATTER = ThreadLocal.withInitial(
            () -> new Formatter(new StringBuilder(4))
    );

    /**
     * Returns a string of the specified octet formatted in {@code 0x02X} form.
     *
     * @param octet the octet to format.
     * @return a formatted string of the {@code octet}.
     */
    public static String formatOctet(final int octet) {
        final var formatter = OCTET_FORMATTER.get();
        final var appendable = (StringBuilder) formatter.out();
//        appendable.delete(0, appendable.length());
        appendable.setLength(0);
        return formatter.format("0x%1$02X", octet & 0xFF).toString();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Logs that a client has connected to the specified remote address through the specified local address.
     *
     * @param remote the remote address connected to.
     * @param local  the local address connected through.
     */
    public static void logConnected(final SocketAddress remote, final SocketAddress local) {
        log.info("connected to {}, through {}", remote, local);
    }

    /**
     * Logs that a server has accepted a client from the specified remote address through the specified local address.
     *
     * @param remote the remote address accepted from.
     * @param local  the local address accepted through.
     */
    public static void logAccepted(final SocketAddress remote, final SocketAddress local) {
        log.info("accepted from {}, through {}", remote, local);
    }

    /**
     * Logs that a socket has received an EOF from the specified remote address.
     *
     * @param remote the remote address from which an EOF received from.
     */
    public static void logReceivedEof(final SocketAddress remote) {
        log.info("received an EOF, from {}", remote);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
