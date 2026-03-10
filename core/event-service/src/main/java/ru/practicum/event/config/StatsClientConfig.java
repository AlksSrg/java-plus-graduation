package ru.practicum.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.client.RecommendationsClient;
import ru.practicum.ewm.client.UserActionClient;

/**
 * Конфигурация для gRPC клиентов рекомендательного сервиса.
 */
@Configuration
public class StatsClientConfig {

    /**
     * Создает бин UserActionClient для отправки действий пользователей.
     */
    @Bean
    public UserActionClient userActionClient() {
        return new UserActionClient();
    }

    /**
     * Создает бин RecommendationsClient для получения рекомендаций.
     */
    @Bean
    public RecommendationsClient recommendationsClient() {
        return new RecommendationsClient();
    }
}