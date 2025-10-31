package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public final class __TestUtils {

//    public static Process startProcess(final Class<?> mainClass) {
//        Objects.requireNonNull(mainClass, "mainClass is null");
//        return __Utils.applyCommandAndClasspath((cmd, cp) -> {
//            try {
//                return new ProcessBuilder(cmd, "-cp", cp, mainClass.getName())
//                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
//                        .start() // IOException
//                        ;
//            } catch (final IOException ioe) {
//                throw new RuntimeException("failed to start " + mainClass, ioe);
//            }
//        });
//    }

    public static Process startProcessAndWriteQuitIn(final Class<?> mainClass, final Duration sleepFor) {
        Objects.requireNonNull(mainClass, "mainClass is null");
        Objects.requireNonNull(sleepFor, "sleepFor is null");
        final var process = __Utils.startProcess(mainClass);
        Thread.ofPlatform().start(() -> {
            try {
                log.debug("sleeping for {}", sleepFor);
                Thread.sleep(sleepFor); // InterruptedException
                __Utils.quitProcess(process);
                __Utils.waitForProcess(process);
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