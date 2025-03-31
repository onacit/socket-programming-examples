package com.github.onacit.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class SocketOptionsUtilsTest {

    @DisplayName("Socket")
    @Test
    void __Socket() throws IOException {
        try (var socket = new Socket()) {
            SocketOptionsUtils.logSocketOptions(socket);
        }
    }

    @DisplayName("ServerSocket")
    @Test
    void __ServerSocket() throws IOException {
        try (var socket = new ServerSocket()) {
            SocketOptionsUtils.logSocketOptions(socket);
        }
    }
}