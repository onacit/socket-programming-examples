package com.github.onacit.rfc768;

/**
 * Constants for the <a href="https://datatracker.ietf.org/doc/html/rfc768">RFC-768</a>.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
public final class __Rfc768_Constants {

    // ----------------------------------------------------------------------------------------------------- Source Port
    public static final int OFFSET_SOURCE_PORT = 0;

    public static final int LENGTH_SOURCE_PORT = 16;

    // ------------------------------------------------------------------------------------------------ Destination Port
    public static final int OFFSET_DESTINATION_PORT = 16;

    public static final int LENGTH_DESTINATION_PORT = 16;

    // ---------------------------------------------------------------------------------------------------------- Length
    public static final int OFFSET_LENGTH_PORT = 32;

    public static final int LENGTH_LENGTH_PORT = 16;

    // -------------------------------------------------------------------------------------------------------- Checksum
    public static final int OFFSET_CHECKSUM_PORT = 48;

    public static final int LENGTH_CHECKSUM_PORT = 16;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int SIZE = 64;

    public static final int BYTES = 8;

    // ------------------------------------------------------------------------------------------------------------ IPv4
    public static final class PseudoHeader {

        // ---------------------------------------------------------------------------------------------- source address
        public static final int OFFSET_SOURCE_ADDRESS = 0;

        public static final int LENGTH_SOURCE_ADDRESS = 32;

        // ----------------------------------------------------------------------------------------- destination address
        public static final int OFFSET_DESTINATION_ADDRESS = 32;

        public static final int LENGTH_DESTINATION_ADDRESS = 32;

        // -------------------------------------------------------------------------------------------------------- zero
        public static final int OFFSET_ZEROS = 64;

        public static final int LENGTH_ZEROS = Byte.SIZE;

        // ---------------------------------------------------------------------------------------------------- protocol
        public static final int OFFSET_PROTOCOL = 72;

        public static final int LENGTH_PROTOCOL = 8;

        // -------------------------------------------------------------------------------------------------- UDP length
        public static final int OFFSET_UDP_LENGTH = 80;

        public static final int LENGTH_UDP_LENGTH = 16;

        // -------------------------------------------------------------------------------------------------------------
        private static final int SIZE = 96;

        private static final int BYTES = 12;

        // -------------------------------------------------------------------------------------------------------------
        public static final int DATA_OCTETS_MAX = 65507;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Rfc768_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
