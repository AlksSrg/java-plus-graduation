package ru.practicum.avro.serialization.exception;

/**
 * Исключение, выбрасываемое при ошибках десериализации Avro.
 */
public class DeserializerException extends RuntimeException {
    public DeserializerException(String message) {
        super(message);
    }

    public DeserializerException(String message, Throwable cause) {
        super(message, cause);
    }
}