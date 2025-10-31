package com.github.onacit.rfc863;

import com.github.onacit.__TestUtils;
import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
abstract class Rfc863Tcp$Client__Test<T extends Rfc863Tcp$Client> {

    Rfc863Tcp$Client__Test(final Class<T> clientClass) {
        super();
        this.clientClass = Objects.requireNonNull(clientClass, "clientClass is null");
    }

    // -----------------------------------------------------------------------------------------------------------------

    // -----------------------------------------------------------------------------------------------------------------
    @DisplayName("start -> exited: true")
    @Test
    void _exited_startAndWriteQuit() throws InterruptedException {
        // -------------------------------------------------------------------------------------------------- given/when
        final var process = __Utils.startProcess(clientClass);
        final var exited = process.waitFor(4L, TimeUnit.SECONDS);
        // -------------------------------------------------------------------------------------------------------- then
        assertThat(exited).isTrue();
    }

    @DisplayName("should destroy when server destroyed")
    @Test
    void shouldDestroyWhenServerDestroyed() throws InterruptedException {
        // -------------------------------------------------------------------- start a process of a random server class
        final var duration = Duration.ofSeconds(4L);
        final var serverProcess = __TestUtils.startProcessAndWriteQuitIn(
                __Rfc863Tcp_Server_TestUtils.randomServerClass(),
                Duration.ofSeconds(4L)
        );
        log.debug("serverProcess: {}", serverProcess);
        // ---------------------------------------------------------------------------- start a process of <clientClass>
        final var clientProcess = __Utils.startProcess(clientClass);
        log.debug("clientProcess: {}", clientProcess);
        // -------------------------------------------------------------------------------- wait for the <clientProcess>
        final var clientExisted = clientProcess.waitFor(16L, TimeUnit.SECONDS);
        log.debug("clientExited: {}", clientExisted);
        assertThat(clientExisted).isTrue();
        // -------------------------------------------------------------------------------- wait for the <serverProcess>
        final var serverExited = serverProcess.waitFor(duration.toSeconds() << 1, TimeUnit.SECONDS);
        log.debug("serverExited: {}", serverExited);
        assertThat(serverExited).isTrue();
    }

    // -----------------------------------------------------------------------------------------------------------------
    final Class<T> clientClass;
}