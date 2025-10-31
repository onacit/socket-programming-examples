package com.github.onacit.rfc864;

import java.nio.ByteBuffer;

/**
 * An interface for generating bytes to write to the clients.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see _Utils#newPatternGenerator()
 */
interface _Generator {

    /**
     * Returns a buffer to write.
     *
     * @return a buffer to write.
     */
    ByteBuffer buffer();
}
