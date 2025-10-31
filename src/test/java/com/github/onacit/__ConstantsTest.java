package com.github.onacit;

import com.github.onacit.rfc768.__Rfc768_Constants;
import com.github.onacit.rfc791.__Rfc791_Constants;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class __ConstantsTest {

    @Test
    void __() {
        assertThat(__Constants.UDP_PAYLOAD_MAX).isEqualTo(
                __Rfc768_Constants.PseudoHeader.DATA_OCTETS_MAX + __Rfc791_Constants.InternetHeaderFormat.BYTES_MIN
        );
    }
}