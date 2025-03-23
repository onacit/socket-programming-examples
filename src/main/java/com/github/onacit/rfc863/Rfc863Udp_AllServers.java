package com.github.onacit.rfc863;

import com.github.onacit.__Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class Rfc863Udp_AllServers {

    static final List<Class<? extends Rfc863Udp$Server>> CLASSES = List.of(
            Rfc863Udp1Server_DatagramSocket.class,
            Rfc863Udp2Server_DatagramChannel_Blocking.class,
            Rfc863Udp3Server_DatagramChannel_NonBlocking.class
    );

    public static void main(final String... args) {
        __Utils.startAll(CLASSES);
    }
}
