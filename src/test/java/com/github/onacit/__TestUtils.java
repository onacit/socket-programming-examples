package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class __TestUtils {

    public static Process startProcess(final Class<?> mainClass) {
        Objects.requireNonNull(mainClass, "mainClass is null");
        return __Utils.applyCommandAndClasspath((cmd, cp) -> {
            try {
                return new ProcessBuilder(cmd, "-cp", cp, mainClass.getName())
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .start() // IOException
                        ;
            } catch (final IOException ioe) {
                throw new RuntimeException("failed to start " + mainClass, ioe);
            }
        });
    }

    public static Process writeQuit(final Process process) throws IOException {
        log.debug("writing quit to {}", process);
        Objects.requireNonNull(process, "process is null");
        process.getOutputStream().write("quit\r\n".getBytes()); // IOException
        process.getOutputStream().flush(); // IOException
        return process;
    }

    public static Process startProcessAndWriteQuitIn(final Class<?> mainClass, final Duration sleepFor) {
        Objects.requireNonNull(mainClass, "mainClass is null");
        Objects.requireNonNull(sleepFor, "sleepFor is null");
        if (sleepFor.isZero() || sleepFor.isNegative()) {
            throw new IllegalArgumentException("sleepFor is not positive: " + sleepFor);
        }
        final Process process = startProcess(mainClass);
        Thread.ofPlatform().start(() -> {
            try {
                log.debug("sleeping for {}", sleepFor);
                Thread.sleep(sleepFor); // InterruptedException
                writeQuit(process);
                final boolean exited = process.waitFor(4L, TimeUnit.SECONDS); // InterruptedException
                if (!exited) {
                    final var forcedToDestroy = process.destroyForcibly();
                    log.debug("forcedToDestroy: {}", forcedToDestroy);
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
        return process;
    }

    private __TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}