package ru.practicum.stats.collector.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;
import ru.practicum.stats.collector.config.CollectorConfig;

import java.time.Duration;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaClientImp implements KafkaClient {
    private Producer<Long, SpecificRecordBase> producer;
    private final CollectorConfig config;

    @Override
    public Producer<Long, SpecificRecordBase> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.flush();
            producer.close(Duration.ofSeconds(10));
        }
    }

    private void initProducer() {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getProducer().getKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getProducer().getValueSerializer());

        // Добавляем Schema Registry
        if (config.getProducer().getProperties() != null &&
                config.getProducer().getProperties().getSchemaRegistryUrl() != null) {
            properties.put("schema.registry.url", config.getProducer().getProperties().getSchemaRegistryUrl());
        }

        // Дополнительные настройки для надежности
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        producer = new KafkaProducer<>(properties);
    }
}