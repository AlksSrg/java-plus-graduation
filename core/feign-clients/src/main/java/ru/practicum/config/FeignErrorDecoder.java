package ru.practicum.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Дешифратор ошибок для Feign-клиентов.
 * Преобразует HTTP ошибки в понятные исключения.
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error occurred while calling {} with status: {}", methodKey, response.status());

        // Здесь можно добавить кастомную обработку разных статусов
        switch (response.status()) {
            case 404:
                return new RuntimeException("Resource not found");
            case 409:
                return new RuntimeException("Conflict occurred");
            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }
}