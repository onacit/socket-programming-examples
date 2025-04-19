package com.github.onacit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@Slf4j
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
public final class __CommandLineArgumentsUtils {

    // -----------------------------------------------------------------------------------------------------------------
    public static Optional<SocketAddress> parseSocketAddress(final int defaultPort, final String... args) {
        final var options = new Options();
        options.addOption("h", "host address", false, "host address to bind/connect on/to");
        options.addOption("p", "port number", false, "port number to bind/connect on/to");
        final var parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException pe) {
            return Optional.empty();
        }
        final var host = Optional.ofNullable(cmd.getOptionValue('h'))
                .map(h -> {
                    try {
                        return InetAddress.getByName(h);
                    } catch (final UnknownHostException uhe) {
                        log.error("failed to get inet address by name: " + h, uhe);
                        return null;
                    }
                })
                .orElse(__Constants.ANY_LOCAL);
        final var port = Optional.ofNullable(cmd.getOptionValue('p'))
                .map(p -> {
                    try {
                        return Integer.parseInt(p);
                    } catch (final NumberFormatException nfe) {
                        return null;
                    }
                })
                .filter(p -> p >= 0 && p <= 65535)
                .orElse(defaultPort);
        return Optional.of(new InetSocketAddress(host, port));
    }

    // -----------------------------------------------------------------------------------------------------------------
    private __CommandLineArgumentsUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
