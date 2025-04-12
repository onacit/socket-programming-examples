package com.github.onacit.rfc864;

import com.github.onacit.__Utils;
import com.github.onacit.rfc864.Rfc864Udp$Client;
import com.github.onacit.rfc864.Rfc864Udp1Client_DatagramSocket;
import com.github.onacit.rfc864.Rfc864Udp2Client_DatagramChannel_Blocking;
import com.github.onacit.rfc864.Rfc864Udp3Client_DatagramChannel_NonBlocking;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class Rfc864Udp_AllClients {

    /**
     * An unmodifiable list of all subclasses of {@link Rfc864Udp$Client}.
     */
    static final List<Class<? extends Rfc864Udp$Client>> CLASSES = List.of(
            Rfc864Udp1Client_DatagramSocket.class,
            Rfc864Udp2Client_DatagramChannel_Blocking.class,
            Rfc864Udp3Client_DatagramChannel_NonBlocking.class
    );

    /**
     * Starts processes of {@link #CLASSES}.
     *
     * @param args an array of command line arguments.
     */
    public static void main(final String... args) {
        __Utils.startAll(CLASSES);
    }

    // -----------------------------------------------------------------------------------------------------------------
    private Rfc864Udp_AllClients() {
        throw new AssertionError("instantiation is not allowed");
    }
}
