package ru.practicum.stats.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;
import ru.practicum.stats.collector.config.CollectorConfig;

import java.time.Duration;
import java.util.Properties;

/**
 * Реализация Kafka клиента.
 * Создаёт и управляет продюсером Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClientImpl implements KafkaClient {

    private final CollectorConfig config;
    private Producer<Long, SpecificRecordBase> producer;

    @Override
    public synchronized Producer<Long, SpecificRecordBase> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    @Override
    public void stop() {
        if (producer != null) {
            log.info("Остановка Kafka продюсера");
            producer.flush();
            producer.close(Duration.ofSeconds(10));
            log.info("Kafka продюсер остановлен");
        }
    }

    private void initProducer() {
        log.info("Инициализация Kafka продюсера с bootstrap.servers = {}", config.getBootstrapServers());

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getProducer().getKeySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getProducer().getValueSerializer());

        // Schema Registry
        if (config.getProducer().getProperties() != null &&
                config.getProducer().getProperties().getSchemaRegistryUrl() != null) {
            props.put("schema.registry.url", config.getProducer().getProperties().getSchemaRegistryUrl());
        }

        // Надёжность
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        producer = new KafkaProducer<>(props);
        log.info("Kafka продюсер успешно создан");
    }
}