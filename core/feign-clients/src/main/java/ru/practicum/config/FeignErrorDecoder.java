package ru.practicum.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;

/**
 * Дешифратор ошибок для Feign-клиентов.
 * Преобразует HTTP-статусы в специфические исключения.
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Ошибка при вызове {}: статус {}", methodKey, response.status());

        return switch (response.status()) {
            case 404 -> new NotFoundResource("Ресурс не найден при вызове " + methodKey);
            case 409 -> new ConflictResource("Конфликт при вызове " + methodKey);
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}