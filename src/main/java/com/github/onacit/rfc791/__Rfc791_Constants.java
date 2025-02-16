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

        static {
            assert OFFSET_IHL == OFFSET_VERSION + LENGTH_VERSION;
        }

        public static final int LENGTH_IHL = 4;

        // --------------------------------------------------------------------------------------------- Type of Service
        public static final int OFFSET_TYPE_OF_SERVICE = 8;

        static {
            assert OFFSET_TYPE_OF_SERVICE == OFFSET_IHL + LENGTH_IHL;
        }

        public static final int LENGTH_TYPE_OF_SERVICE = 8;

        // ---------------------------------------------------------------------------------------------- Total_Length
        public static final int OFFSET_TOTAL_LENGTH = 16;

        static {
            assert OFFSET_TOTAL_LENGTH == OFFSET_TYPE_OF_SERVICE + LENGTH_TYPE_OF_SERVICE;
        }

        public static final int LENGTH_TOTAL_LENGTH = 16;

        // ---------------------------------------------------------------------------------------------- Identification
        public static final int OFFSET_IDENTIFICATION = 32;

        static {
            assert OFFSET_IDENTIFICATION == OFFSET_TYPE_OF_SERVICE + LENGTH_TYPE_OF_SERVICE;
        }

        public static final int LENGTH_IDENTIFICATION = 16;

        // ------------------------------------------------------------------------------------------------------- Flags
        public static final int OFFSET_FLAGS = 48;

        static {
            assert OFFSET_FLAGS == OFFSET_IDENTIFICATION + LENGTH_IDENTIFICATION;
        }

        public static final int LENGTH_FLAGS = 3;

        // --------------------------------------------------------------------------------------------- Fragment Offset
        public static final int OFFSET_FRAGMENT_OFFSET = 51;

        static {
            assert OFFSET_FRAGMENT_OFFSET == OFFSET_FLAGS + LENGTH_FLAGS;
        }

        public static final int LENGTH_FRAGMENT_OFFSET = 13;

        // ------------------------------------------------------------------------------------------------ Time to Live
        public static final int OFFSET_TIME_TO_LIVE = 64;

        static {
            assert OFFSET_TIME_TO_LIVE == OFFSET_FRAGMENT_OFFSET + LENGTH_FRAGMENT_OFFSET;
        }

        public static final int LENGTH_TIME_TO_LIVE = 8;

        // ---------------------------------------------------------------------------------------------------- Protocol
        public static final int OFFSET_PROTOCOL = 72;

        static {
            assert OFFSET_PROTOCOL == OFFSET_TIME_TO_LIVE + LENGTH_TIME_TO_LIVE;
        }

        public static final int LENGTH_PROTOCOL = 8;

        // --------------------------------------------------------------------------------------------- Header Checksum
        public static final int OFFSET_HEADER_CHECKSUM = 80;

        static {
            assert OFFSET_HEADER_CHECKSUM == OFFSET_PROTOCOL + LENGTH_PROTOCOL;
        }

        public static final int LENGTH_HEADER_CHECKSUM = 16;

        // ---------------------------------------------------------------------------------------------- Source Address
        public static final int OFFSET_SOURCE_ADDRESS = 96;

        static {
            assert OFFSET_SOURCE_ADDRESS == OFFSET_HEADER_CHECKSUM + LENGTH_HEADER_CHECKSUM;
        }

        public static final int LENGTH_SOURCE_ADDRESS = 32;

        // ----------------------------------------------------------------------------------------- Destination Address
        public static final int OFFSET_DESTINATION_ADDRESS = 128;

        static {
            assert OFFSET_DESTINATION_ADDRESS == OFFSET_SOURCE_ADDRESS + LENGTH_SOURCE_ADDRESS;
        }

        public static final int LENGTH_DESTINATION_ADDRESS = 32;

        // ----------------------------------------------------------------------------------------------------- Options
        public static final int OFFSET_OPTIONS = 160;

        static {
            assert OFFSET_OPTIONS == OFFSET_DESTINATION_ADDRESS + LENGTH_DESTINATION_ADDRESS;
        }

        public static final int LENGTH_OPTIONS = 24;

        // ----------------------------------------------------------------------------------------------------- Padding
        public static final int OFFSET_PADDING = 184;

        static {
            assert OFFSET_PADDING == OFFSET_OPTIONS + LENGTH_OPTIONS;
        }

        public static final int LENGTH_PADDING = 8;

        // -------------------------------------------------------------------------------------------------------------
        public static final int SIZE = 192;

        static {
            assert SIZE == OFFSET_PADDING + LENGTH_PADDING;
        }

        public static final int BYTES = 24;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Rfc791_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
