package ru.practicum.event.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import ru.practicum.StatsClient;

/**
 * Конфигурация для StatsClient.
 * Обеспечивает создание бина StatsClient для взаимодействия со статистикой.
 */
@Configuration
public class StatsClientConfig {

    @Value("${stats-client.id:stats-service}")
    private String statsServerId;

    /**
     * Создает бин StatsClient с использованием DiscoveryClient и RetryTemplate.
     *
     * @param discoveryClient клиент для обнаружения сервисов
     * @param retryTemplate   шаблон для повторных попыток
     * @return настроенный StatsClient
     */
    @Bean
    public StatsClient statsClient(DiscoveryClient discoveryClient, RetryTemplate retryTemplate) {
        return new StatsClient(statsServerId, discoveryClient, retryTemplate);
    }

    /**
     * Создает бин RetryTemplate для повторных попыток при сбоях.
     *
     * @return настроенный RetryTemplate
     */
    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(1000, 2, 5000)
                .build();
    }
}