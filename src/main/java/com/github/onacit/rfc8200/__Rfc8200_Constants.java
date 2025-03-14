package com.github.onacit.rfc8200;

/**
 * Constants of <a href="https://datatracker.ietf.org/doc/html/rfc8200">RFC-8200 Internet Protocol, Version 6 (IPv6)  *
 * Specification</a>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8200">RFC-8200 Internet Protocol, Version 6 (IPv6)
 * Specification</a>
 */
public final class __Rfc8200_Constants {

    /**
     * Constants defined on <a href="https://datatracker.ietf.org/doc/html/rfc8200#section-3">3.  IPv6 Header
     * Format</a>.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8200#section-3">3.  IPv6 Header Format</a> (<a
     * href="https://datatracker.ietf.org/doc/html/rfc8200">RFC 8200 Internet Protocol, Version 6 (IPv6)
     * Specification</a>)
     */
    public static final class Ipv6HeaderFormat {

        // ----------------------------------------------------------------------------------------------------- Version
        public static final int OFFSET_VERSION = 0;

        public static final int LENGTH_VERSION = 4;

        public static final int VALUE_VERSION = 0b1100; // 6

        // ----------------------------------------------------------------------------------------------- Traffic Class
        public static final int OFFSET_TRAFFIC_CLASS = 4;

        public static final int LENGTH_TRAFFIC_CLASS = 8;

        // -------------------------------------------------------------------------------------------------- Flow Label
        public static final int OFFSET_FLOW_LABEL = 12;

        public static final int LENGTH_FLOW_LABEL = 20;

        // ---------------------------------------------------------------------------------------------- Payload Length
        public static final int OFFSET_PAYLOAD_LENGTH = 32;

        public static final int LENGTH_PAYLOAD_LENGTH = 16;

        // ------------------------------------------------------------------------------------------------- Next Header
        public static final int OFFSET_NEXT_HEADER_LENGTH = 46;

        public static final int LENGTH_NEXT_HEADER_LENGTH = 8;

        // --------------------------------------------------------------------------------------------------- Hop Limit
        public static final int OFFSET_HOP_LIMIT_LENGTH = 54;

        public static final int LENGTH_HOP_LIMIT_LENGTH = 8;

        // ---------------------------------------------------------------------------------------------- Source Address
        public static final int OFFSET_SOURCE_ADDRESS = 62;

        public static final int LENGTH_SOURCE_ADDRESS = 128;

        // ----------------------------------------------------------------------------------------- Destination Address
        public static final int OFFSET_DESTINATION_ADDRESS = 190;

        public static final int LENGTH_DESTINATION_ADDRESS = 128;

        // -------------------------------------------------------------------------------------------------------------
        public static final int SIZE = 320;

        public static final int BYTES = 40;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Rfc8200_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
