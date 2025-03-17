package com.github.onacit.rfc864;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;

@Slf4j
class Rfc864Tcp5Server_AsynchronousServerSocketChannel {

    public static void main(final String... args) throws IOException, InterruptedException {
        try (var server = AsynchronousServerSocketChannel.open()) {
            {
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
                } catch (final Exception e) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEADDR, e);
                }
                try {
                    server.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
                } catch (final Exception e) {
                    log.error("failed to set {}", StandardSocketOptions.SO_REUSEPORT, e);
                }
            }
            throw new UnsupportedOperationException("not implemented");
        }
    }
}
