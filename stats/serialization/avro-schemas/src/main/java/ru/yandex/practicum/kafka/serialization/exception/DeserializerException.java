package ru.yandex.practicum.kafka.serialization.exception;

public class DeserializerException extends RuntimeException {
    public DeserializerException(String msg) {
        super(msg);
    }

    public DeserializerException(String message, Throwable cause){
        super(message, cause);
    }
}