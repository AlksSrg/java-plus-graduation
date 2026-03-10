package ru.practicum.stats.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Главный класс приложения Collector.
 * Запускает gRPC сервер для сбора действий пользователей и отправляет их в Kafka.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GrpcUserActionApp {

    public static void main(String[] args) {
        SpringApplication.run(GrpcUserActionApp.class, args);
    }
}