package ru.practicum.stats.collector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Kafka для коллектора.
 * Содержит настройки bootstrap-серверов, продюсера и топиков.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class CollectorConfig {

    private String bootstrapServers;
    private ProducerConfig producer;
    private TopicConfig topic;

    @Getter
    @Setter
    public static class ProducerConfig {
        private String keySerializer;
        private String valueSerializer;
        private PropertiesConfig properties;
    }

    @Getter
    @Setter
    public static class PropertiesConfig {
        private String schemaRegistryUrl;
    }

    @Getter
    @Setter
    public static class TopicConfig {
        private String userActions;
    }
}