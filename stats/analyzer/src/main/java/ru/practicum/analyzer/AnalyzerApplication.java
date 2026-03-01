package ru.practicum.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Главный класс приложения Analyzer.
 */
@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class AnalyzerApplication {

    /**
     * Точка входа в приложение Analyzer.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        log.info("Запуск сервиса Analyzer...");
        SpringApplication.run(AnalyzerApplication.class, args);
        log.info("Сервис Analyzer успешно запущен");
    }
}