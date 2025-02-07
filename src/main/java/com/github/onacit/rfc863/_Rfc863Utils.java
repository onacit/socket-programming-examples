package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

@Slf4j
final class _Rfc863Utils {

    static void readQuit() throws IOException {
        final var reader = new BufferedReader(new InputStreamReader(System.in));
        for (String line; (line = reader.readLine()) != null; ) {
            if (line.toUpperCase().contains("QUIT")) {
                break;
            }
        }
    }

    static void readQuitAndCall(final Callable<?> callable) {
        Thread.ofPlatform().name("quit-reader").daemon().start(() -> {
            try {
                readQuit();
            } catch (final IOException ioe) {
                log.error("failed to read quit", ioe);
            } finally {
                try {
                    callable.call();
                } catch (final Exception e) {
                    log.debug("failed to call {}", callable, e);
                }
            }
        });
    }

    static void readQuitAndClose(final Closeable closeable) {
        readQuitAndCall(() -> {
            closeable.close();
            return null;
        });
    }

    private _Rfc863Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
