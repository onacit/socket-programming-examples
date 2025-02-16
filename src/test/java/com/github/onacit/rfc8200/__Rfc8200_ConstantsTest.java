package com.github.onacit.rfc8200;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Slf4j
class __Rfc8200_ConstantsTest {

    @Nested
    class Ipv6HeaderFormatTest {

        @Test
        void __() {
            log.debug("size: {}, bytes: {}", __Rfc8200_Constants.Ipv6HeaderFormat.SIZE,
                      __Rfc8200_Constants.Ipv6HeaderFormat.BYTES);
        }
    }
}