package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Класс для формирования стандартизированного ответа с ошибкой.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String status;
    private final String message;
    private final String reason;
    private final LocalDateTime timestamp;
    private final Map<String, String> errors;
    private final String path;

    private ErrorResponse(String status, String message, String reason,
                          LocalDateTime timestamp, Map<String, String> errors, String path) {
        this.status = status;
        this.message = message;
        this.reason = reason;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.errors = errors;
        this.path = path;
    }

    /**
     * Создает базовый ответ с ошибкой.
     *
     * @param status  HTTP статус
     * @param message сообщение об ошибке
     * @param reason  причина ошибки
     * @return объект ErrorResponse
     */
    public static ErrorResponse of(String status, String message, String reason) {
        return new ErrorResponse(status, message, reason, LocalDateTime.now(), null, null);
    }

    /**
     * Создает ответ с ошибкой и путем.
     *
     * @param status  HTTP статус
     * @param message сообщение об ошибке
     * @param reason  причина ошибки
     * @param path    путь запроса
     * @return объект ErrorResponse
     */
    public static ErrorResponse of(String status, String message, String reason, String path) {
        return new ErrorResponse(status, message, reason, LocalDateTime.now(), null, path);
    }

    /**
     * Создает ответ с ошибкой валидации.
     *
     * @param status  HTTP статус
     * @param message сообщение об ошибке
     * @param errors  карта ошибок валидации
     * @param path    путь запроса
     * @return объект ErrorResponse
     */
    public static ErrorResponse validationError(String status, String message, Map<String, String> errors, String path) {
        return new ErrorResponse(status, message, "Validation failed", LocalDateTime.now(), errors, path);
    }

    /**
     * Создает новый Builder.
     *
     * @return новый Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder для ErrorResponse.
     */
    public static class Builder {
        private String status;
        private String message;
        private String reason;
        private LocalDateTime timestamp;
        private Map<String, String> errors;
        private String path;

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder errors(Map<String, String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(status, message, reason, timestamp, errors, path);
        }
    }
}