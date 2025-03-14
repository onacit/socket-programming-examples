package com.github.onacit.rfc791;

/**
 * Constants of <a href="https://datatracker.ietf.org/doc/html/rfc791">RFC-791 INTERNET PROTOCOL DARPA INTERNET PROGRAM
 * PROTOCOL SPECIFICATION September 1981</a>.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc791">RFC-791 INTERNET PROTOCOL DARPA INTERNET PROGRAM PROTOCOL
 * SPECIFICATION September 1981</a>
 */
public final class __Rfc791_Constants {

    /**
     * Constants defined in <a href="https://datatracker.ietf.org/doc/html/rfc791#section-3.1">3.1.  Internet Header
     * Format</a>.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc791#section-3.1">3.1.  Internet Header Format</a>
     */
    public static final class InternetHeaderFormat {

        // ----------------------------------------------------------------------------------------------------- Version
        public static final int OFFSET_VERSION = 0;

        public static final int LENGTH_VERSION = 4;

        public static final int VALUE_VERSION = 0b1000; // 4

        // --------------------------------------------------------------------------------------------------------- IHL
        public static final int OFFSET_IHL = 4;

        public static final int LENGTH_IHL = 4;

        // --------------------------------------------------------------------------------------------- Type of Service
        public static final int OFFSET_TYPE_OF_SERVICE = 8;

        public static final int LENGTH_TYPE_OF_SERVICE = 8;

        // ---------------------------------------------------------------------------------------------- Total_Length
        public static final int OFFSET_TOTAL_LENGTH = 16;

        public static final int LENGTH_TOTAL_LENGTH = 16;

        // ---------------------------------------------------------------------------------------------- Identification
        public static final int OFFSET_IDENTIFICATION = 32;

        public static final int LENGTH_IDENTIFICATION = 16;

        // ------------------------------------------------------------------------------------------------------- Flags
        public static final int OFFSET_FLAGS = 48;

        public static final int LENGTH_FLAGS = 3;

        // --------------------------------------------------------------------------------------------- Fragment Offset
        public static final int OFFSET_FRAGMENT_OFFSET = 51;

        public static final int LENGTH_FRAGMENT_OFFSET = 13;

        // ------------------------------------------------------------------------------------------------ Time to Live
        public static final int OFFSET_TIME_TO_LIVE = 64;

        public static final int LENGTH_TIME_TO_LIVE = 8;

        // ---------------------------------------------------------------------------------------------------- Protocol
        public static final int OFFSET_PROTOCOL = 72;

        public static final int LENGTH_PROTOCOL = 8;

        // --------------------------------------------------------------------------------------------- Header Checksum
        public static final int OFFSET_HEADER_CHECKSUM = 80;

        public static final int LENGTH_HEADER_CHECKSUM = 16;

        // ---------------------------------------------------------------------------------------------- Source Address
        public static final int OFFSET_SOURCE_ADDRESS = 96;

        public static final int LENGTH_SOURCE_ADDRESS = 32;

        // ----------------------------------------------------------------------------------------- Destination Address
        public static final int OFFSET_DESTINATION_ADDRESS = 128;

        public static final int LENGTH_DESTINATION_ADDRESS = 32;

        // ----------------------------------------------------------------------------------------------------- Options
        public static final int OFFSET_OPTIONS = 160;

        // -----------------------------------------------------------------------------------------------------------------
        public static final int SIZE_MIN = OFFSET_OPTIONS;

        public static final int BYTES_MIN = SIZE_MIN >> 3;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Rfc791_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
