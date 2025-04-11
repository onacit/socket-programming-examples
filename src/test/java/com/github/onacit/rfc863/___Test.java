package com.github.onacit.rfc863;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

@Slf4j
class ___Test {

    @Test
    void __() throws Exception {
        final var latch = new CountDownLatch(1);
        var server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress(InetAddress.getLocalHost(), 10001));
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                try {
                    log.debug("accepted from {}", result.getRemoteAddress());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    result.shutdownOutput();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                result.read(ByteBuffer.allocate(1), null, new CompletionHandler<Integer, Object>() {
                    @Override
                    public void completed(Integer result, Object attachment) {
                        log.debug("read: {}", result);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        throw new RuntimeException(exc);
                    }
                });
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
            }
        });
        // -------------------------------------------------------------------------------------------------------------
        var client = AsynchronousSocketChannel.open();
        client.connect(
                new InetSocketAddress(InetAddress.getLocalHost(), 10001),
                null,
                new CompletionHandler<Void, Object>() {
                    @Override
                    public void completed(Void result, Object attachment) {
                        try {
                            log.debug("connected to {}", client.getRemoteAddress());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        client.write(ByteBuffer.allocate(1), null, new CompletionHandler<Integer, Object>() {
                            @Override
                            public void completed(Integer result, Object attachment) {
                                log.debug("written: {}", result);
                            }

                            @Override
                            public void failed(Throwable exc, Object attachment) {
                                throw new RuntimeException(exc);
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        throw new RuntimeException(exc);
                    }
                });
        // -----------------------------------------------------------------------------------------------------------------
        latch.await();
    }
}
