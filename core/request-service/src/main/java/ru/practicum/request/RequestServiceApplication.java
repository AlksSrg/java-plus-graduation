package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Главный класс приложения для микросервиса управления запросами на участие.
 * Обеспечивает запуск Spring Boot приложения, регистрацию в Eureka и сканирование компонентов.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ru.practicum.feignclients.client")
@ComponentScan(basePackages = {"ru.practicum.request", "ru.practicum.exception"})
public class RequestServiceApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}