package ru.practicum.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Главный класс приложения User Service.
 * Отвечает за управление пользователями (администрирование).
 * Предоставляет API для создания, получения и удаления пользователей.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ru.practicum.feignclients")
@ComponentScan(basePackages = {"ru.practicum.user", "ru.practicum.exception.handler", "ru.practicum.handler"})
public class UserServiceApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}