package com.github.onacit.rfc863;

import com.github.onacit.__TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

    @DisplayName("should destroy when server destroyed")
    @Test
    void shouldDestroyWhenServerDestroyed() throws IOException, InterruptedException {
        // -------------------------------------------------------------------------------------------------------------
        final var serverProcess = __TestUtils.startProcessAndWriteQuitIn(
                __Rfc863Tcp_Server_TestUtils.randomServerClass(),
                Duration.ofSeconds(4L)
        );
        log.debug("serverProcess: {}", serverProcess);
        // -------------------------------------------------------------------------------------------------------------
        final var clientProcess = __TestUtils.startProcess(
                __Rfc863Tcp_Client_TestUtils.randomClientClass()
        );
        log.debug("clientProcess: {}", clientProcess);
        final var existed = clientProcess.waitFor(8L, TimeUnit.SECONDS);
        // -------------------------------------------------------------------------------------------------------------
        assertThat(existed).isTrue();
    }

    final Class<T> clientClass;
}