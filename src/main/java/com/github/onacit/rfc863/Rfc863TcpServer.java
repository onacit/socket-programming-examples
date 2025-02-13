package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc863TcpServer {

    public static void main(final String... args) {
        final var classes = List.of(
                Rfc863Tcp1Server.class,
                Rfc863Tcp2Server.class,
                Rfc863Tcp3Server.class,
                Rfc863Tcp4Server.class
        );
        _Utils.acceptCommandAndClasspath((command, classpath) -> {
            final var processes = classes.stream()
                    .map(c -> new ProcessBuilder(command, "-cp", classpath, c.getName())
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
            _Utils.readQuitAndRun(() -> {
                processes.forEach(p -> {
                    try {
                        p.getOutputStream().write("quit\r\n".getBytes());
                        p.getOutputStream().flush();
                    } catch (final IOException ioe) {
                        throw new RuntimeException("failed to write quit to " + p, ioe);
                    }
                });
                processes.forEach(p -> {
                    try {
                        final var exited = p.waitFor(10L, TimeUnit.SECONDS);
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
