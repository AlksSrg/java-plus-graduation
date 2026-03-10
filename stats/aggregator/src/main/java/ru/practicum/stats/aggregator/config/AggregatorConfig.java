package ru.practicum.stats.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Конфигурация Kafka для агрегатора.
 * Содержит настройки подключения, потребителя и топиков.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka")
public class AggregatorConfig {

    private String bootstrapServers;
    private ConsumerConfig consumer;
    private ProducerConfig producer;
    private TopicsConfig topics;

    @Getter
    @Setter
    public static class ConsumerConfig {
        private String groupId;
        private String keyDeserializer;
        private String valueDeserializer;
        private String autoOffsetReset;
        private Boolean enableAutoCommit;
    }

    @Getter
    @Setter
    public static class ProducerConfig {
        private String keySerializer;
        private String valueSerializer;
    }

    @Getter
    @Setter
    public static class TopicsConfig {
        private String userActions;
        private String eventsSimilarity;
    }
}