package com.github.onacit.rfc863;

import com.github.onacit.__TestUtils;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class Rfc863Udp_AllServersTest {

    @Test
    void __() throws InterruptedException {
        final var duration = Duration.ofSeconds(2L);
        final var process = __TestUtils.startProcessAndWriteQuitIn(Rfc863Udp_AllServers.class, duration);
        final var exited = process.waitFor(2L, TimeUnit.SECONDS);
        assertThat(exited).isTrue();
    }
}