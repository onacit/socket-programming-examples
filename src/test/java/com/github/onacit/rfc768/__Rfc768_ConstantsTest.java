package com.github.onacit.rfc768;

import com.github.onacit.rfc791.__Rfc791_Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class __Rfc768_ConstantsTest {

    @Test
    void __() {
        log.debug("SIZE: {}, BYTES: {}", __Rfc768_Constants.SIZE, __Rfc768_Constants.BYTES);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @DisplayName("PseudoHeader")
    @Nested
    class PseudoHeaderTest {

        static {
            log.debug(__Rfc768_Constants.PseudoHeader.class.getName());
        }

        @DisplayName("DATA_OCTETS_MAX")
        @Test
        void __() {

            log.debug("DATA_OCTETS_MAX: {}", __Rfc768_Constants.PseudoHeader.DATA_OCTETS_MAX);
            assertThat(__Rfc768_Constants.PseudoHeader.DATA_OCTETS_MAX).isEqualTo(
                    0xFFFF - (__Rfc768_Constants.BYTES + __Rfc791_Constants.InternetHeaderFormat.BYTES_MIN)
            );
        }
    }
}