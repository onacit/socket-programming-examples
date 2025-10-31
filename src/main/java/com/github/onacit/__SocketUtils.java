package com.github.onacit;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
@Slf4j
public final class __SocketUtils {

    // ------------------------------------------------------------------------------------------------- java.net.Socket
    public static <T extends Socket> T SO_REUSEADDR(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEADDR, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEADDR, s, v, uhe);
        }
        return s;
    }

    public static <T extends Socket> T SO_REUSEADDR(final T s) throws IOException {
        return SO_REUSEADDR(s, Boolean.TRUE);
    }

    public static <T extends Socket> T SO_REUSEPORT(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEPORT, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEPORT, s, v, uhe);
        }
        return s;
    }

    public static <T extends Socket> T SO_REUSEPORT(final T s) throws IOException {
        return SO_REUSEPORT(s, Boolean.TRUE);
    }

    // ------------------------------------------------------------------------------------------- java.net.ServerSocket
    public static <T extends ServerSocket> T SO_REUSEADDR(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(v, "v is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEADDR, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEADDR, s, v, uhe);
        }
        return s;
    }

    public static <T extends ServerSocket> T SO_REUSEADDR(final T s) throws IOException {
        return SO_REUSEADDR(s, Boolean.TRUE);
    }

    public static <T extends ServerSocket> T SO_REUSEPORT(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(v, "v is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEPORT, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEPORT, s, v, uhe);
        }
        return s;
    }

    public static <T extends ServerSocket> T SO_REUSEPORT(final T s) throws IOException {
        return SO_REUSEPORT(s, Boolean.TRUE);
    }

    // --------------------------------------------------------------------------------- java.nio.channels.SocketChannel
    public static <T extends SocketChannel> T SO_REUSEADDR(final T s, final Boolean v)
            throws IOException {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(v, "v is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEADDR, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEADDR, s, v, uhe);
        }
        return s;
    }

    public static <T extends SocketChannel> T SO_REUSEPORT(final T s, final Boolean v)
            throws IOException {
        Objects.requireNonNull(s, "s is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEPORT, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEPORT, s, v, uhe);
        }
        return s;
    }

    // --------------------------------------------------------------------------- java.nio.channels.ServerSocketChannel
    public static <T extends ServerSocketChannel> T SO_REUSEADDR(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(v, "v is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEADDR, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEADDR, s, v, uhe);
        }
        return s;
    }

    public static <T extends ServerSocketChannel> T SO_REUSEADDR_ON(final T s) throws IOException {
        return SO_REUSEADDR(s, Boolean.TRUE);
    }

    public static <T extends ServerSocketChannel> T SO_REUSEPORT(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEPORT, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEPORT, s, v, uhe);
        }
        return s;
    }

    public static <T extends ServerSocketChannel> T SO_REUSEPORT(final T s) throws IOException {
        return SO_REUSEPORT(s, Boolean.TRUE);
    }

    // --------------------------------------------------------------------- java.nio.channels.AsynchronousSocketChannel
    public static <T extends AsynchronousSocketChannel> T SO_REUSEADDR(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(v, "v is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEADDR, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEADDR, s, v, uhe);
        }
        return s;
    }

    public static <T extends AsynchronousSocketChannel> T SO_REUSEPORT(final T s, final Boolean v) throws IOException {
        Objects.requireNonNull(s, "s is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEPORT, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEPORT, s, v, uhe);
        }
        return s;
    }

    // --------------------------------------------------------------- java.nio.channels.AsynchronousServerSocketChannel
    public static <T extends AsynchronousServerSocketChannel> T SO_REUSEADDR(final T s, final Boolean v)
            throws IOException {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(v, "v is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEADDR, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEADDR, s, v, uhe);
        }
        return s;
    }

    public static <T extends AsynchronousServerSocketChannel> T SO_REUSEPORT(final T s, final Boolean v)
            throws IOException {
        Objects.requireNonNull(s, "s is null");
        try {
            s.setOption(StandardSocketOptions.SO_REUSEPORT, v); // IOException
        } catch (final UnsupportedOperationException uhe) {
            log.error("failed to set {} v {} with {}", StandardSocketOptions.SO_REUSEPORT, s, v, uhe);
        }
        return s;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __SocketUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
