package com.github.onacit.rfc863;

import com.github.onacit.__TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
abstract class Rfc863Udp$Client__Test<T extends Rfc863Udp$Client> {

    Rfc863Udp$Client__Test(final Class<T> clientClass) {
        super();
        this.clientClass = Objects.requireNonNull(clientClass, "clientClass is null");
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Test
    void _quit_quit() throws InterruptedException {
        final var process = __TestUtils.startProcessAndWriteQuitIn(clientClass, Duration.ofSeconds(2L));
        final var exited = process.waitFor(4L, TimeUnit.SECONDS);
        assertThat(exited).isTrue();
    }

    // -----------------------------------------------------------------------------------------------------------------
    final Class<T> clientClass;
}