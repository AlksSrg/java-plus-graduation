package ru.practicum.stats.aggregator.kafka;

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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClientImpl implements KafkaClient {

    private final AggregatorConfig config;
    private Producer<String, EventSimilarityAvro> producer;
    private Consumer<String, UserActionAvro> consumer;

    @Override
    public Producer<String, EventSimilarityAvro> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    @Override
    public Consumer<String, UserActionAvro> getConsumer() {
        if (consumer == null) {
            initConsumer();
        }
        return consumer;
    }

    @Override
    public void stop() {
        if (producer != null) {
            log.info("Закрытие продюсера");
            producer.close(Duration.ofSeconds(10));
        }

        if (consumer != null) {
            log.info("Закрытие консьюмера");
            consumer.close();
        }
    }

    /**
     * Инициализация продюсера.
     */
    private void initProducer() {
        log.info("Создание продюсера");

        // ДИАГНОСТИКА
        System.out.println("========== ДИАГНОСТИКА PRODUCER ==========");
        System.out.println("bootstrapServers: " + config.getBootstrapServers());
        System.out.println("==========================================");

        Properties props = new Properties();

        String bootstrapServers = config.getBootstrapServers();
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            bootstrapServers = "localhost:9092";
            log.warn("bootstrapServers не найден, используем default: {}", bootstrapServers);
        }
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "ru.practicum.ewm.stats.avro.serializer.EventSimilarityAvroSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        producer = new KafkaProducer<>(props);
        log.info("Продюсер создан");
    }

    /**
     * Инициализация консьюмера.
     */
    private void initConsumer() {
        log.info("Создание консьюмера");

        // ДИАГНОСТИКА
        System.out.println("========== ДИАГНОСТИКА CONSUMER ==========");
        System.out.println("bootstrapServers: " + config.getBootstrapServers());

        if (config.getConsumer() == null) {
            System.err.println("❌ config.getConsumer() is NULL!");
            throw new IllegalStateException("consumer config is null");
        }

        System.out.println("consumer.groupId: " + config.getConsumer().getGroupId());
        System.out.println("consumer.keyDeserializer: " + config.getConsumer().getKeyDeserializer());
        System.out.println("consumer.valueDeserializer: " + config.getConsumer().getValueDeserializer());
        System.out.println("consumer.autoOffsetReset: " + config.getConsumer().getAutoOffsetReset());
        System.out.println("consumer.enableAutoCommit: " + config.getConsumer().getEnableAutoCommit());
        System.out.println("==========================================");

        Properties props = new Properties();

        // bootstrapServers
        String bootstrapServers = config.getBootstrapServers();
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            bootstrapServers = "localhost:9092";
            log.warn("bootstrapServers не найден, используем default: {}", bootstrapServers);
        }
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // groupId
        String groupId = config.getConsumer().getGroupId();
        if (groupId == null || groupId.isEmpty()) {
            groupId = "aggregator-group";
            log.warn("groupId не найден, используем default: {}", groupId);
        }
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // keyDeserializer
        String keyDeser = config.getConsumer().getKeyDeserializer();
        if (keyDeser == null || keyDeser.isEmpty()) {
            keyDeser = "org.apache.kafka.common.serialization.StringDeserializer";
            log.warn("keyDeserializer не найден, используем default: {}", keyDeser);
        }
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeser);

        // valueDeserializer
        String valueDeser = config.getConsumer().getValueDeserializer();
        if (valueDeser == null || valueDeser.isEmpty()) {
            valueDeser = "ru.practicum.ewm.stats.avro.deserializer.UserActionAvroDeserializer";
            log.warn("valueDeserializer не найден, используем default: {}", valueDeser);
        }
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeser);

        // autoOffsetReset
        String autoOffsetReset = config.getConsumer().getAutoOffsetReset();
        if (autoOffsetReset == null || autoOffsetReset.isEmpty()) {
            autoOffsetReset = "earliest";
            log.warn("autoOffsetReset не найден, используем default: {}", autoOffsetReset);
        }
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        // enableAutoCommit
        Boolean autoCommit = config.getConsumer().getEnableAutoCommit();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit != null ? autoCommit : false);

        consumer = new KafkaConsumer<>(props);
        log.info("Консьюмер создан");
    }
}