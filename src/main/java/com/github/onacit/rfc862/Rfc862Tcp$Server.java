package com.github.onacit.rfc862;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * An abstract class for {@code TCP} servers receive requests from {@link Rfc862Tcp$Client} clients.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
abstract class Rfc862Tcp$Server {

}
