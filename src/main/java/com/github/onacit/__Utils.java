package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

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
     * Keep reading lines from the {@link System#in standard input stream} until it reads a line contains {@code quit},
     * and {@link Callable#call() calls} specified task.
     *
     * @param daemon
     * @param callable the task to {@link Callable#call() call}.
     */
    public static void readQuitAndCall(final boolean daemon, final Callable<?> callable) {
        Objects.requireNonNull(callable, "callable is null");
        Thread.ofPlatform().name("read-quit-and-call").daemon(daemon).start(() -> {
            try {
                readQuit();
            } catch (final IOException ioe) {
                log.error("failed to read quit", ioe);
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
     * Keep reading lines from the {@link System#in standard input stream} until it reads a line contains {@code quit},
     * and {@link Runnable#run() runs} specified task.
     *
     * @param daemon
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
     * Keep reading lines from the {@link System#in standard input stream} until it reads a line contains {@code quit},
     * and {@link Closeable#close() closes} specified closeable.
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
                    log.debug("closing {}", closeable);
                    closeable.close();
                    return null;
                }
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void acceptCommandAndClasspath(final BiConsumer<? super String, ? super String> consumer) {
        Objects.requireNonNull(consumer, "consumer is null");
        final var info = ProcessHandle.current().info();
        final var command = info.command().orElseThrow();
        log.debug("command: {}", command);
        final String classpath;
        {
            String cp = null;
            final var arguments = info.arguments().orElseThrow();
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].equals("-classpath")) {
                    cp = arguments[i + 1];
                    break;
                }
            }
            classpath = cp;
        }
        log.debug("classpath: {}", Optional.ofNullable(classpath).orElseThrow());
        consumer.accept(command, classpath);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
