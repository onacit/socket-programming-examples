package com.github.onacit.rfc8200;

/**
 * Constants of <a href="https://datatracker.ietf.org/doc/html/rfc8200">RFC-8200 Internet Protocol, Version 6 (IPv6)  *
 * Specification</a>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8200">RFC-8200 Internet Protocol, Version 6 (IPv6)
 * Specification</a>
 */
public final class __Rfc8200_Constants {

    public static final class Ipv6HeaderFormat {

        // ----------------------------------------------------------------------------------------------------- Version
        public static final int OFFSET_VERSION = 0;

        public static final int LENGTH_VERSION = 4;

        public static final int VALUE_VERSION = 0b1100; // 6

        // ----------------------------------------------------------------------------------------------- Traffic Class
        public static final int OFFSET_TRAFFIC_CLASS = 4;

        static {
            assert OFFSET_TRAFFIC_CLASS == OFFSET_VERSION + LENGTH_VERSION;
        }

        public static final int LENGTH_TRAFFIC_CLASS = 8;

        // -------------------------------------------------------------------------------------------------- Flow Label
        public static final int OFFSET_FLOW_LABEL = 12;

        static {
            assert OFFSET_FLOW_LABEL == OFFSET_TRAFFIC_CLASS + LENGTH_TRAFFIC_CLASS;
        }

        public static final int LENGTH_FLOW_LABEL = 20;

        // ---------------------------------------------------------------------------------------------- Payload Length
        public static final int OFFSET_PAYLOAD_LENGTH = 32;

        static {
            assert OFFSET_PAYLOAD_LENGTH == OFFSET_FLOW_LABEL + LENGTH_FLOW_LABEL;
        }

        public static final int LENGTH_PAYLOAD_LENGTH = 16;

        // ------------------------------------------------------------------------------------------------- Next Header
        public static final int OFFSET_NEXT_HEADER_LENGTH = 46;

        static {
            assert OFFSET_NEXT_HEADER_LENGTH == OFFSET_PAYLOAD_LENGTH + LENGTH_PAYLOAD_LENGTH;
        }

        public static final int LENGTH_NEXT_HEADER_LENGTH = 8;

        // --------------------------------------------------------------------------------------------------- Hop Limit
        public static final int OFFSET_HOP_LIMIT_LENGTH = 54;

        static {
            assert OFFSET_HOP_LIMIT_LENGTH == OFFSET_NEXT_HEADER_LENGTH + LENGTH_NEXT_HEADER_LENGTH;
        }

        public static final int LENGTH_HOP_LIMIT_LENGTH = 8;

        // ---------------------------------------------------------------------------------------------- Source Address
        public static final int OFFSET_SOURCE_ADDRESS_LENGTH = 62;

        static {
            assert OFFSET_SOURCE_ADDRESS_LENGTH == OFFSET_HOP_LIMIT_LENGTH + LENGTH_HOP_LIMIT_LENGTH;
        }

        public static final int LENGTH_SOURCE_ADDRESS_LENGTH = 128;

        // ----------------------------------------------------------------------------------------- Destination Address
        public static final int OFFSET_DESTINATION_ADDRESS_LENGTH = 190;

        static {
            assert OFFSET_DESTINATION_ADDRESS_LENGTH == OFFSET_SOURCE_ADDRESS_LENGTH + LENGTH_SOURCE_ADDRESS_LENGTH;
        }

        public static final int LENGTH_DESTINATION_ADDRESS_LENGTH = 128;

        // -----------------------------------------------------------------------------------------------------------------
        public static final int SIZE = 320;

        static {
            assert SIZE == OFFSET_DESTINATION_ADDRESS_LENGTH + LENGTH_DESTINATION_ADDRESS_LENGTH;
        }

        public static final int BYTES = 40;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Rfc8200_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
