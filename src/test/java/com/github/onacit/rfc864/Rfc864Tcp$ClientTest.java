package com.github.onacit.rfc864;

import java.util.Objects;

class Rfc864Tcp$ClientTest<T extends Rfc864Tcp$Client> {

    Rfc864Tcp$ClientTest(final Class<T> clientClass) {
        super();
        this.clientClass = Objects.requireNonNull(clientClass, "clientClass is null");
    }

    final Class<T> clientClass;
}