package ru.practicum.stats.aggregator.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.config.AggregatorConfig;

import java.time.Duration;
import java.util.Properties;

/**
 * Реализация Kafka клиента.
 * Создаёт и управляет продюсером и консьюмером.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClientImpl implements KafkaClient {

    private final AggregatorConfig config;
    private Producer<String, EventSimilarityAvro> producer;
    private Consumer<String, UserActionAvro> consumer;

    @Override
    public synchronized Producer<String, EventSimilarityAvro> getProducer() {
        if (producer == null) {
            producer = createProducer();
        }
        return producer;
    }

    @Override
    public synchronized Consumer<String, UserActionAvro> getConsumer() {
        if (consumer == null) {
            consumer = createConsumer();
        }
        return consumer;
    }

    @Override
    @PreDestroy
    public void stop() {
        log.info("Закрытие Kafka клиента");
        if (producer != null) {
            producer.close(Duration.ofSeconds(5));
            log.info("Продюсер закрыт");
        }
        if (consumer != null) {
            consumer.close();
            log.info("Консьюмер закрыт");
        }
    }

    private Producer<String, EventSimilarityAvro> createProducer() {
        log.info("Создание продюсера с bootstrap.servers = {}", config.getBootstrapServers());
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "ru.practicum.ewm.stats.avro.serializer.EventSimilarityAvroSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return new KafkaProducer<>(props);
    }

    private Consumer<String, UserActionAvro> createConsumer() {
        log.info("Создание консьюмера с bootstrap.servers = {}", config.getBootstrapServers());
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());

        AggregatorConfig.ConsumerConfig consumerCfg = config.getConsumer();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerCfg.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerCfg.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerCfg.getValueDeserializer());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerCfg.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                consumerCfg.getEnableAutoCommit() != null ? consumerCfg.getEnableAutoCommit() : false);
        return new KafkaConsumer<>(props);
    }
}