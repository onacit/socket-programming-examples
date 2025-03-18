package com.github.onacit.rfc864;

import java.util.Objects;

class Rfc864Tcp$ServerTest<T extends Rfc864Tcp$Server> {

    Rfc864Tcp$ServerTest(final Class<T> serverClass) {
        super();
        this.serverClass = Objects.requireNonNull(serverClass, "serverClass is null");
    }

    final Class<T> serverClass;
}