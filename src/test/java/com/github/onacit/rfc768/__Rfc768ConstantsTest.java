package com.github.onacit.rfc768;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class __Rfc768ConstantsTest {

    @Test
    void __IPv4() {
        log.debug("header size: {} bits ({} bytes", __Rfc768_Constants.IPv4PseudoHeader.HEADER_SIZE,
                  __Rfc768_Constants.IPv4PseudoHeader.HEADER_BYTES);
        log.debug("max data bytes: {}", __Rfc768_Constants.IPv4PseudoHeader.DATA_BYTES_MAX);
    }

    @Test
    void __IPv6() {
        log.debug("header size: {} bits ({} bytes", __Rfc768_Constants.IPv6PseudoHeader.HEADER_SIZE,
                  __Rfc768_Constants.IPv6PseudoHeader.HEADER_BYTES);
        log.debug("max data bytes: {}", __Rfc768_Constants.IPv6PseudoHeader.DATA_BYTES_MAX);
    }
}