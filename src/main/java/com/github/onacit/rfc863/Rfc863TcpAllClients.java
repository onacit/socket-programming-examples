package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
class Rfc863TcpAllClients {

    public static void main(final String... args) {
        final var classes = List.of(
                Rfc863Tcp1Client.class,
                Rfc863Tcp2Client.class,
                Rfc863Tcp3Client.class,
                Rfc863Tcp4Client.class,
                Rfc863Tcp5Client.class
        );
        __Utils.acceptCommandAndClasspath((command, classpath) -> {
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
            __Utils.readQuitAndRun(true, () -> {
                processes.forEach(p -> {
                    try {
                        p.getOutputStream().write("quit\r\n".getBytes());
                        p.getOutputStream().flush();
                    } catch (final IOException ioe) {
                        throw new RuntimeException("failed to write quit to " + p, ioe);
                    }
                });
//                processes.forEach(p -> {
//                    try {
//                        final var exited = p.waitFor(10L, TimeUnit.SECONDS);
//                        assert exited;
//                        log.debug("process: {}", p);
//                    } catch (final InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                        throw new RuntimeException("interrupted while waiting for " + p, ie);
//                    }
//                });
            });
            processes.forEach(p -> {
                try {
                    final var exitValue = p.waitFor(); // InterruptedException
                    log.debug("exitValue: {}, process: {}", exitValue, p);
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("interrupted while waiting for " + p, ie);
                }
            });
        });
    }
}
