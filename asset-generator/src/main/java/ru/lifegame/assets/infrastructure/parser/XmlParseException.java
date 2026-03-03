package ru.lifegame.assets.infrastructure.parser;

/**
 * Thrown when an XML asset-spec file cannot be read or is structurally
 * invalid.
 */
public class XmlParseException extends RuntimeException {

    public XmlParseException(String message) {
        super(message);
    }

    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
