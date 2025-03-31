package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
public final class __Utils {

    static void readQuit(final BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader, "reader is null");
        for (String l; (l = reader.readLine()) != null; ) {
            if (l.toUpperCase().contains("QUIT")) {
                break;
            }
        }
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
     * Keep reading lines from the {@link System#in standard input stream} until it reads a line contains {@code quit}.
     *
     * @throws IOException if an I/O error occurs.
     */
    static void readQuit() throws IOException {
        readQuit(System.in);
    }

    /**
     * Starts a new thread which keeps reading lines from the {@link System#in standard input stream} until it reads a
     * line contains (case-insensitively) '{@code quit}, and then {@link Callable#call() calls} specified task.
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
     * line contains (case-insensitively) '{@code quit}', and then {@link Runnable#run() runs} specified task.
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
     * line contains (case-insensitively) '{@code quit}', and then {@link Closeable#close() closes} specified
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
                daemon,
                () -> {
                    closeable.close();
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
        Objects.requireNonNull(buffer, "buffer is null");

//        buffer.position(buffer.position() + ThreadLocalRandom.current().nextInt(buffer.remaining() + 1));
//        buffer.limit(buffer.limit() - ThreadLocalRandom.current().nextInt(buffer.remaining() + 1));

        buffer.limit(ThreadLocalRandom.current().nextInt(buffer.capacity() + 1));
        buffer.position(ThreadLocalRandom.current().nextInt(buffer.remaining() + 1));

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
    private __Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
