package com.github.onacit.rfc768;

/**
 * Constants for the <a href="https://datatracker.ietf.org/doc/html/rfc768">RFC-768</a>.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
public final class __Rfc768_Constants {

    // -----------------------------------------------------------------------------------------------------------------
    public static final int OFFSET_SOURCE_PORT = 0;

    public static final int SIZE_SOURCE_PORT = Short.SIZE;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int OFFSET_DESTINATION_PORT = 16;

    static {
        assert OFFSET_DESTINATION_PORT == OFFSET_SOURCE_PORT + SIZE_SOURCE_PORT;
    }

    public static final int SIZE_DESTINATION_PORT = Short.SIZE;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int OFFSET_LENGTH_PORT = Integer.SIZE;

    static {
        assert OFFSET_LENGTH_PORT == OFFSET_DESTINATION_PORT + SIZE_DESTINATION_PORT;
    }

    public static final int SIZE_LENGTH_PORT = Short.SIZE;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int OFFSET_CHECKSUM_PORT = 48;

    static {
        assert OFFSET_CHECKSUM_PORT == OFFSET_LENGTH_PORT + SIZE_LENGTH_PORT;
    }

    public static final int SIZE_CHECKSUM_PORT = Short.SIZE;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int SIZE_HEADER = 64;

    static {
        assert SIZE_HEADER == OFFSET_CHECKSUM_PORT + SIZE_CHECKSUM_PORT;
    }

    public static final int BYTES_HEADER = 8;

    static {
        assert BYTES_HEADER == SIZE_HEADER >> 3;
    }

    // ------------------------------------------------------------------------------------------------------------ IPv4
    public static final class IPv4PseudoHeader {

        public static final int OFFSET_SOURCE_ADDRESS = 0;

        public static final int SIZE_SOURCE_ADDRESS = Integer.SIZE;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_DESTINATION_ADDRESS = 32;

        static {
            assert OFFSET_DESTINATION_ADDRESS == OFFSET_SOURCE_ADDRESS + SIZE_SOURCE_ADDRESS;
        }

        public static final int SIZE_DESTINATION_ADDRESS = Integer.SIZE;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_ZEROS = 64;

        static {
            assert OFFSET_ZEROS == OFFSET_DESTINATION_ADDRESS + SIZE_DESTINATION_ADDRESS;
        }

        public static final int SIZE_ZEROS = Byte.SIZE;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_PROTOCOL = 72;

        static {
            assert OFFSET_PROTOCOL == OFFSET_ZEROS + SIZE_ZEROS;
        }

        public static final int SIZE_PROTOCOL = Byte.SIZE;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_UDP_LENGTH = 80;

        static {
            assert OFFSET_UDP_LENGTH == OFFSET_PROTOCOL + SIZE_PROTOCOL;
        }

        public static final int SIZE_UDP_LENGTH = Short.SIZE;

        // -------------------------------------------------------------------------------------------------------------
        private static final int SIZE_HEADER = 96;

        static {
            assert SIZE_HEADER == OFFSET_UDP_LENGTH + SIZE_UDP_LENGTH;
        }

        private static final int BYTES_HEADER = 12;

        static {
            assert BYTES_HEADER == SIZE_HEADER >> 3;
        }

        // -------------------------------------------------------------------------------------------------------------
        static final int HEADER_SIZE = 160;

        static {
            assert HEADER_SIZE == SIZE_HEADER + __Rfc768_Constants.SIZE_HEADER;
        }

        public static int HEADER_BYTES = 20;

        static {
            assert HEADER_BYTES == BYTES_HEADER + __Rfc768_Constants.BYTES_HEADER;
        }

        // -------------------------------------------------------------------------------------------------------------
        public static final int DATA_BYTES_MAX = 65507;

        static {
            assert DATA_BYTES_MAX == 0xFFFF - HEADER_BYTES - __Rfc768_Constants.BYTES_HEADER;
        }
    }

    // ------------------------------------------------------------------------------------------------------------ IPv6
    public static final class IPv6PseudoHeader {

        // -------------------------------------------------------------------------------------------------------------
        private static final int OFFSET_SOURCE_ADDRESS = 0;

        private static final int LENGTH_SOURCE_ADDRESS = Integer.SIZE << 2;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_DESTINATION_ADDRESS = 128;

        static {
            assert OFFSET_DESTINATION_ADDRESS == OFFSET_SOURCE_ADDRESS + LENGTH_SOURCE_ADDRESS;
        }

        public static final int LENGTH_DESTINATION_ADDRESS = Integer.SIZE << 2;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_UDP_LENGTH = 256;

        static {
            assert OFFSET_UDP_LENGTH == OFFSET_DESTINATION_ADDRESS + LENGTH_DESTINATION_ADDRESS;
        }

        public static final int LENGTH_UDP_LENGTH = Integer.SIZE;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_ZEROS = 288;

        static {
            assert OFFSET_ZEROS == OFFSET_UDP_LENGTH + LENGTH_UDP_LENGTH;
        }

        public static final int LENGTH_ZEROS = 24;

        // -------------------------------------------------------------------------------------------------------------
        public static final int OFFSET_NEXT = 312;

        static {
            assert OFFSET_NEXT == OFFSET_ZEROS + LENGTH_ZEROS;
        }

        public static final int LENGTH_NEXT = Byte.SIZE;

        // -------------------------------------------------------------------------------------------------------------
        private static final int SIZE_HEADER = 320;

        static {
            assert SIZE_HEADER == OFFSET_NEXT + LENGTH_NEXT;
        }

        private static final int BYTES_HEADER = 40;

        static {
            assert BYTES_HEADER == SIZE_HEADER >> 3;
        }

        // -------------------------------------------------------------------------------------------------------------
        static final int HEADER_SIZE = 384;

        static {
            assert HEADER_SIZE == SIZE_HEADER + __Rfc768_Constants.SIZE_HEADER;
        }

        public static int HEADER_BYTES = 48;

        static {
            assert HEADER_BYTES == HEADER_SIZE >> 3;
            assert HEADER_BYTES == BYTES_HEADER + __Rfc768_Constants.BYTES_HEADER;
        }

        // -------------------------------------------------------------------------------------------------------------
        public static final int DATA_BYTES_MAX = 65479;

        static {
            assert DATA_BYTES_MAX == 0xFFFF - HEADER_BYTES - __Rfc768_Constants.BYTES_HEADER;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __Rfc768_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
