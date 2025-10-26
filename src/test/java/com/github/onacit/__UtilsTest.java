package com.github.onacit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
class __UtilsTest {

    @Nested
    class FormatOctetTest {

        @Test
        void __() {
            ThreadLocalRandom.current().ints(10).forEach(o -> {
                final var formatted = __Utils.formatOctet(o);
                log.debug("octet: {}, formatted: {}", String.format("%1$08x", o), formatted);
            });
        }
    }
}
