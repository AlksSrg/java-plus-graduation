package ru.yandex.practicum.kafka.serialization.exception;

public class SerializerException extends RuntimeException{
    public SerializerException(String msg) {
        super(msg);
    }

    public SerializerException(String message, Throwable cause){
        super(message, cause);
    }
}