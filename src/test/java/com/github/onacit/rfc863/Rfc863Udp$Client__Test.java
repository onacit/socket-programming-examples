package com.github.onacit.rfc863;

import com.github.onacit.__TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An abstract class for testing subclasses of {@link Rfc863Udp$Client} class.
 *
 * @param <T> subclass type parameter
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
abstract class Rfc863Udp$Client__Test<T extends Rfc863Udp$Client> {

    /**
     * Creates a new instance with the specified client class.
     *
     * @param clientClass the client class to test.
     */
    Rfc863Udp$Client__Test(final Class<T> clientClass) {
        super();
        this.clientClass = Objects.requireNonNull(clientClass, "clientClass is null");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Starts a process of {@link #clientClass}, writes {@code 'quit'} to the process, and asserts that the process
     * exits normally.
     *
     * @throws InterruptedException when interrupted while waiting for the process.
     */
    @DisplayName("start -> 'quit' -> exited")
    @Test
    void _exited_startAndWriteQuit() throws InterruptedException {
        // ------------------------------------------------------------------------------------------------ given / when
        final var duration = Duration.ofSeconds(2L);
        final var process = __TestUtils.startProcessAndWriteQuitIn(clientClass, duration);
        final var exited = process.waitFor(duration.toSeconds() << 1, TimeUnit.SECONDS);
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(exited).isTrue();
    }

    // -----------------------------------------------------------------------------------------------------------------
    final Class<T> clientClass;
}