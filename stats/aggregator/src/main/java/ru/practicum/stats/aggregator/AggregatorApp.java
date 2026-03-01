package ru.practicum.stats.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.stats.aggregator.handler.AggregationStarter;

/**
 * Главный класс агрегатора.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class AggregatorApp {

    /**
     * Точка входа.
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(AggregatorApp.class, args);

        context.getBean(AggregationStarter.class).start();
    }
}