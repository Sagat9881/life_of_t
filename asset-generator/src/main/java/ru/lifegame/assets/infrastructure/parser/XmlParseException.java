package ru.lifegame.assets.infrastructure.parser;

/**
 * Thrown when XML asset specification parsing fails.
 */
public class XmlParseException extends RuntimeException {

    public XmlParseException(String message) {
        super(message);
    }

    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
