package ru.practicum.stats.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Kafka для агрегатора.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class AggregatorConfig {

    /**
     * Адреса Kafka брокеров.
     */
    private String bootstrapServers;

    /**
     * Настройки потребителя.
     */
    private ConsumerConfig consumer;

    /**
     * Названия топиков.
     */
    private TopicsConfig topics;

    /**
     * Конфигурация потребителя Kafka.
     */
    @Getter
    @Setter
    public static class ConsumerConfig {
        /**
         * ID группы потребителей.
         */
        private String groupId;

        /**
         * Класс десериализатора ключей.
         */
        private String keyDeserializer;

        /**
         * Класс десериализатора значений.
         */
        private String valueDeserializer;

        /**
         * Стратегия сброса смещений.
         */
        private String autoOffsetReset;

        /**
         * Флаг авто-подтверждения.
         */
        private Boolean enableAutoCommit;
    }

    /**
     * Названия Kafka топиков.
     */
    @Getter
    @Setter
    public static class TopicsConfig {
        /**
         * Топик действий пользователей.
         */
        private String userActions;

        /**
         * Топик схожести мероприятий.
         */
        private String eventsSimilarity;
    }
}