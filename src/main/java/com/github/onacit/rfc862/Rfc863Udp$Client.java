package com.github.onacit.rfc862;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * An abstract class for {@code UDP} clients send requests to {@link Rfc863Udp$Server} servers.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
abstract class Rfc863Udp$Client {

}
