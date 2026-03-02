package ru.practicum.analyzer.consumer;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.avro.serialization.EventSimilarityDeserializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;

/**
 * Потребитель Kafka для получения данных о схожести мероприятий.
 */
@Slf4j
@Service
public class EventSimilarityConsumerService {

    private final KafkaConsumer<Long, SpecificRecordBase> kafkaConsumer;

    public EventSimilarityConsumerService(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${kafka.group-id.similarity}") String groupId,
            @Value("${kafka.auto-commit}") boolean autoCommit
    ) {
        this.kafkaConsumer = new KafkaConsumer<>(createConsumerConfig(bootstrapServers, groupId, autoCommit));
    }

    private Properties createConsumerConfig(String bootstrapServers, String groupId, boolean autoCommit) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        return props;
    }

    public ConsumerRecords<Long, SpecificRecordBase> poll(Duration duration) {
        try {
            return kafkaConsumer.poll(duration);
        } catch (WakeupException e) {
            log.info("Wakeup exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при poll: ", e);
            throw e;
        }
    }

    public void subscribe(Collection<String> topics) {
        kafkaConsumer.subscribe(topics);
        log.info("Подписка на топики: {}", topics);
    }

    public void commitAsync() {
        kafkaConsumer.commitAsync((offsets, exception) -> {
            if (exception != null) {
                log.error("Ошибка commitAsync: {}", offsets, exception);
            }
        });
    }

    public void wakeup() {
        kafkaConsumer.wakeup();
    }

    @PreDestroy
    public void close() {
        log.info("Закрытие Kafka consumer");
        kafkaConsumer.close();
    }
}