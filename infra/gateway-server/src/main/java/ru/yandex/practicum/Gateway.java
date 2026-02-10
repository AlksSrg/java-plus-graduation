package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Главный класс Gateway приложения.
 * Используется как точка входа в API Gateway.
 */
@SpringBootApplication(scanBasePackages = "ru.yandex.practicum.gateway")
@EnableDiscoveryClient
public class Gateway {
    /**
     * Точка входа в приложение Gateway.
     */
    public static void main(String[] args) {
        SpringApplication.run(Gateway.class, args);
    }
}