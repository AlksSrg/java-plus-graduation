package ru.practicum.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Главный класс микросервиса для управления подборками событий.
 * Обеспечивает запуск Spring Boot приложения, регистрацию в Eureka
 * и сканирование компонентов, включая Feign-клиенты из общего модуля.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ru.practicum.feignclients")
@ComponentScan(basePackages = {"ru.practicum.compilation", "ru.practicum.exception"})
@ConfigurationPropertiesScan
public class CompilationServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CompilationServiceApp.class, args);
    }
}