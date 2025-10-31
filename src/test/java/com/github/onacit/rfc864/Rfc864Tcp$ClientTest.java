package com.github.onacit.rfc864;

import com.github.onacit.__TestUtils;
import com.github.onacit.__Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

class Rfc864Tcp$ClientTest<T extends Rfc864Tcp$Client> {

    Rfc864Tcp$ClientTest(final Class<T> clientClass) {
        super();
        this.clientClass = Objects.requireNonNull(clientClass, "clientClass is null");
    }

//    // -----------------------------------------------------------------------------------------------------------------
//    @DisplayName("should destroy when server destroyed")
//    @Test
//    void shouldDestroyWhenServerDestroyed() throws InterruptedException {
//        // -------------------------------------------------------------------- start a process of a random server class
//        final var serverProcess = __TestUtils.startProcessAndWriteQuitIn(
//                __Rfc864Tcp_Server_TestUtils.randomServerClass(),
//                Duration.ofSeconds(4L)
//        );
//        log.debug("serverProcess: {}", serverProcess);
//        // ---------------------------------------------------------------------------- start a process of <clientClass>
//        final var clientProcess = __Utils.startProcess(clientClass);
//        log.debug("clientProcess: {}", clientProcess);
//        final var existed = clientProcess.waitFor(8L, TimeUnit.SECONDS);
//        // -------------------------------------------------------------------------------------------------------------
//        assertThat(existed).isTrue();
//    }

    // -----------------------------------------------------------------------------------------------------------------
    final Class<T> clientClass;
}