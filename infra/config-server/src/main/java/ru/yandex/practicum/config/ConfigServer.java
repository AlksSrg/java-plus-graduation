package ru.yandex.practicum.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Основной класс сервера конфигурации.
 * Предоставляет централизованное управление конфигурациями для микросервисов.
 * Поддерживает динамическое обновление конфигураций без перезапуска приложений.
 */
@EnableConfigServer
@SpringBootApplication
@EnableDiscoveryClient
public class ConfigServer {

    /**
     * Точка входа в сервер конфигурации.
     * Запускает Spring Boot приложение, предоставляющее конфигурации для других сервисов.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}