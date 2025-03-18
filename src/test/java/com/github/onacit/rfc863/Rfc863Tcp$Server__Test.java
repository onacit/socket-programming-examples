package com.github.onacit.rfc863;

import com.github.onacit.__TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
abstract class Rfc863Tcp$Server__Test<T extends Rfc863Tcp$Server> {

    Rfc863Tcp$Server__Test(final Class<T> serverClass) {
        super();
        this.serverClass = Objects.requireNonNull(serverClass, "serverClass is null");
    }

    @Test
    void _quit_quit() throws InterruptedException {
        final var process = __TestUtils.startProcessAndWriteQuitIn(serverClass, Duration.ofSeconds(2L));
        final var exited = process.waitFor(4L, TimeUnit.SECONDS);
        assertThat(exited).isTrue();
    }

    final Class<T> serverClass;
}