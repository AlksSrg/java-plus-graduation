package ru.practicum.avro.serialization.exception;

/**
 * Исключение, выбрасываемое при ошибках сериализации Avro.
 */
public class SerializerException extends RuntimeException {
    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }
}