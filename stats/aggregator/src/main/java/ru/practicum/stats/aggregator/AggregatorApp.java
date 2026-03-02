package ru.practicum.stats.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Главный класс агрегатора.
 * Запускает Spring Boot приложение и включает сканирование конфигурационных свойств.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class AggregatorApp {

    public static void main(String[] args) {
        SpringApplication.run(AggregatorApp.class, args);
    }
}