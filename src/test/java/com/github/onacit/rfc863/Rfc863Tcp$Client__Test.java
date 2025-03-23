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
    @DisplayName("should destroy when server destroyed")
    @Test
    void shouldDestroyWhenServerDestroyed() throws InterruptedException {
        // -------------------------------------------------------------------- start a process of a random server class
        final var serverProcess = __TestUtils.startProcessAndWriteQuitIn(
                __Rfc863Tcp_Server_TestUtils.randomServerClass(),
                Duration.ofSeconds(4L)
        );
        log.debug("serverProcess: {}", serverProcess);
        // ---------------------------------------------------------------------------- start a process of <clientClass>
        final var clientProcess = __Utils.startProcess(clientClass);
        log.debug("clientProcess: {}", clientProcess);
        final var existed = clientProcess.waitFor(8L, TimeUnit.SECONDS);
        // -------------------------------------------------------------------------------------------------------------
        assertThat(existed).isTrue();
    }

    // -----------------------------------------------------------------------------------------------------------------
    final Class<T> clientClass;
}