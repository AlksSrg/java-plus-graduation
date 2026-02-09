package ru.yandex.practicum.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Основной класс Eureka Server (Discovery Server).
 * Запускает сервер обнаружения сервисов для микросервисной архитектуры.
 * Предоставляет реестр всех доступных сервисов и их экземпляров.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServer {

    /**
     * Точка входа в Eureka Server.
     * Инициализирует Spring Boot приложение, предоставляющее
     * сервис обнаружения для всех микросервисов Smart Home.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServer.class, args);
    }
}