package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class Rfc863Udp_AllClients {

    /**
     * An unmodifiable list of all subclasses extend {@link Rfc863Udp$Client}.
     */
    static final List<Class<? extends Rfc863Udp$Client>> CLASSES = List.of(
            Rfc863Udp1Client_DatagramSocket.class,
            Rfc863Udp2Client_DatagramChannel_Blocking.class,
            Rfc863Udp3Client_DatagramChannel_NonBlocking.class
    );

    public static void main(final String... args) {
        __Utils.startAll(CLASSES);
    }
}
