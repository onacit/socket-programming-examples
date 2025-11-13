package com.github.onacit.rfc862;

/**
 * Constants for the <a href="https://www.rfc-editor.org/rfc/rfc862">Request for Comments: 862 Echo Protocol</a>.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc862">Request for Comments: 862 Echo Protocol</a>
 */
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
public final class __Rfc862_Constants {

    /**
     * The port number for both TCP and UDP. The value is {@value}.
     */
    public static final int PORT = 7;

    // ------------------------------------------------------------------------------------------------------ CONSTRUCTORS

    /**
     * Creates a new instance, my ass.
     */
    private __Rfc862_Constants() {
        throw new AssertionError("instantiation is not allowed");
    }
}
