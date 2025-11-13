package com.github.onacit.rfc862;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * An abstract class for {@code TCP} clients send requests to {@link Rfc862Tcp$Server} servers.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
abstract class Rfc862Tcp$Client {

}
