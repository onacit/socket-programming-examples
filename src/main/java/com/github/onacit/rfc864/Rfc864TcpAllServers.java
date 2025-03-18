package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc864TcpAllServers {

    public static void main(final String... args) {
        final var classes = List.of(
                Rfc864Tcp1Server_ServerSocket.class,
                Rfc864Tcp2Server_ServerSocketChannel_Blocking.class
//                ,
//                Rfc864Tcp3Server.class,
//                Rfc864Tcp4Server.class
        );
        __Utils.acceptCommandAndClasspath((cmd, cp) -> {
            final var processes = classes.stream()
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
