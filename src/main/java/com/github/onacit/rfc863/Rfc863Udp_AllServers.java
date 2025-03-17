package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
class Rfc863Udp_AllServers {

    public static void main(final String... args) {
        final var classes = List.of(
                Rfc863Udp1Server_DatagramSocket.class,
                Rfc863Udp2Server_DatagramChannel_Blocking.class,
                Rfc863Udp3Server_DatagramChannel_NonBlocking.class
        );
        __Utils.acceptCommandAndClasspath((cmd, cp) -> {
            // start all processes
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
            // read 'quit' and kill all processes
            __Utils.readQuitAndRun(false, () -> {
                // write 'quit\r\n' to each process
                processes.forEach(p -> {
                    try {
                        p.getOutputStream().write("quit\r\n".getBytes());
                        p.getOutputStream().flush();
                    } catch (final IOException ioe) {
                        throw new RuntimeException("failed to write quit to " + p, ioe);
                    }
                });
                // wait for each process to exit
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
