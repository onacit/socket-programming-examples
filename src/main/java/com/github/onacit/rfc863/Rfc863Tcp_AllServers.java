package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc863Tcp_AllServers {

    /**
     * An unmodifiable list of all subclasses of {@link _Rfc863Tcp_Server}.
     */
    static final List<Class<? extends _Rfc863Tcp_Server>> CLASSES = List.of(
            Rfc863Tcp1Server_ServerSocket.class,
            Rfc863Tcp2Server_ServerSocketChannel_Blocking.class,
            Rfc863Tcp3Server_ServerSocketChannel_NonBlocking.class,
            Rfc863Tcp5Server_AsynchronousServerSocketChannel.class,
            Rfc863Tcp4Server_AsynchronousServerSocketChannel_Future.class
    );

    public static void main(final String... args) {
        __Utils.acceptCommandAndClasspath((cmd, cp) -> {
            final var processes = CLASSES.stream()
                    .map(c -> new ProcessBuilder(cmd, "-cp", cp, c.getName())
                            .redirectOutput(ProcessBuilder.Redirect.INHERIT))
                    .map(b -> {
                        try {
                            return b.start();
                        } catch (final IOException ioe) {
                            throw new RuntimeException("failed to start " + b.command(), ioe);
                        }
                    })
                    .peek(p -> {
                        log.debug("process: {}", p.info());
                    })
                    .toList();
            __Utils.readQuitAndRun(false, () -> {
                processes.forEach(p -> {
                    try {
                        p.getOutputStream().write("quit\r\n".getBytes()); // IOException
                        p.getOutputStream().flush(); // IOException
                    } catch (final IOException ioe) {
                        throw new RuntimeException("failed to write/flush 'quit' to " + p, ioe);
                    }
                });
                processes.forEach(p -> {
                    try {
                        final var exited = p.waitFor(10L, TimeUnit.SECONDS); // InterruptedException
                        assert exited;
                        log.debug("process: {}", p);
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("interrupted while waiting for " + p, ie);
                    }
                });
            });
        });
    }
}
