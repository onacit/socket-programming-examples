package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
final class __Rfc863Tcp_Client_TestUtils {

    static Class<? extends Rfc863Tcp$Client> randomClientClass() {
        assert !__Rfc863Tcp_Client_TestConstants.CLIENT_CLASSES.isEmpty();
        return __Rfc863Tcp_Client_TestConstants.CLIENT_CLASSES.get(
                ThreadLocalRandom.current().nextInt(__Rfc863Tcp_Client_TestConstants.CLIENT_CLASSES.size())
        );
    }

    static void startClientAndQuitIn(final Class<? extends Rfc863Tcp$Client> clientClass, final Duration duration) {
        Objects.requireNonNull(clientClass, "clientClass is null");
        Objects.requireNonNull(duration, "duration is null");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration is not positive: " + duration);
        }
        __Utils.acceptCommandAndClasspath((cmd, cp) -> {
            final Process process;
            try {
                process = new ProcessBuilder(cmd, "-cp", cp, clientClass.getName())
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .start() // IOException
                ;
            } catch (final IOException ioe) {
                log.error("failed to start {}", clientClass);
                throw new RuntimeException(ioe);
            }
            log.debug("process: {}", process);
            try {
                Thread.sleep(duration); // InterruptedException
            } catch (final InterruptedException ie) {
                log.error("interrupted while sleeping for {}", duration, ie);
                Thread.currentThread().interrupt();
                final var forcedToDestroy = process.destroyForcibly();
                log.debug("forced to destroy: {}", forcedToDestroy);
                throw new RuntimeException(ie);
            }
            try {
                process.getOutputStream().write("quit\r\n".getBytes()); // IOException
                process.getOutputStream().flush(); // IOException
            } catch (final IOException ioe) {
                log.error("failed to write/flush 'quit' to {}", process, ioe);
                final var forcedToDestroy = process.destroyForcibly();
                log.debug("forced to destroy: {}", forcedToDestroy);
                throw new RuntimeException(ioe);
            }
            boolean exited = true;
            try {
                exited = process.waitFor(8L, TimeUnit.SECONDS); // InterruptedException
            } catch (final InterruptedException ie) {
                log.error("interrupted while waiting {}", process);
                Thread.currentThread().interrupt();
                final var forcedToDestroy = process.destroyForcibly();
                log.debug("forced to destroy: {}", forcedToDestroy);
                throw new RuntimeException(ie);
            }
            if (!exited) {
                final var forcedToDestroy = process.destroyForcibly();
                log.debug("forcedToDestroy: {}", forcedToDestroy);
            }
        });
    }

    static void startRandomClientAndQuitIn(final Duration duration) {
        final Class<? extends Rfc863Tcp$Client> clientClass = randomClientClass();
        startClientAndQuitIn(clientClass, duration);
    }

    private __Rfc863Tcp_Client_TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}