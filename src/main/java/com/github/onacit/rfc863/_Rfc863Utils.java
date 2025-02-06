package com.github.onacit.rfc863;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

final class _Rfc863Utils {

    static void readQuit() throws IOException {
        final var reader = new BufferedReader(new InputStreamReader(System.in));
        for (String line; (line = reader.readLine()) != null; ) {
            if (line.contains("quit")) {
                break;
            }
        }
    }

    static <V> V readQuitAndCall(final Callable<V> callable) throws Exception {
        readQuit();
        return callable.call();
    }

    static void readQuitAndClose(final Closeable closeable) throws Exception {
        readQuitAndCall(() -> {
            closeable.close();
            return null;
        });
    }


    private _Rfc863Utils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
