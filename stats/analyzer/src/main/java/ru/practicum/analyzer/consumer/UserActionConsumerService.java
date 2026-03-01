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
import ru.practicum.ewm.stats.avro.deserializer.UserActionAvroDeserializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;

/**
 * Потребитель Kafka для получения данных о действиях пользователей.
 */
@Slf4j
@Service
public class UserActionConsumerService {

    private final KafkaConsumer<Long, SpecificRecordBase> kafkaConsumer;

    /**
     * Конструктор потребителя для топика действий пользователей.
     *
     * @param bootstrapServers адреса серверов Kafka
     * @param groupId          идентификатор группы
     * @param autoCommit       флаг авто-подтверждения смещений
     */
    public UserActionConsumerService(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${kafka.group-id.actions}") String groupId,
            @Value("${kafka.auto-commit}") boolean autoCommit
    ) {
        this.kafkaConsumer = new KafkaConsumer<>(createConsumerConfig(bootstrapServers, groupId, autoCommit));
    }

    /**
     * Создает конфигурацию для Kafka потребителя.
     *
     * @param bootstrapServers адреса серверов
     * @param groupId          группа потребителей
     * @param autoCommit       авто-подтверждение
     * @return объект Properties с настройками
     */
    private Properties createConsumerConfig(String bootstrapServers, String groupId, boolean autoCommit) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        return props;
    }

    /**
     * Получает записи из Kafka с указанным таймаутом.
     *
     * @param duration время ожидания
     * @return полученные записи
     */
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

    /**
     * Подписывает потребителя на топики.
     *
     * @param topics список топиков
     */
    public void subscribe(Collection<String> topics) {
        kafkaConsumer.subscribe(topics);
        log.info("Подписка на топики: {}", topics);
    }

    /**
     * Асинхронно подтверждает смещения.
     */
    public void commitAsync() {
        kafkaConsumer.commitAsync((offsets, exception) -> {
            if (exception != null) {
                log.error("Ошибка commitAsync: {}", offsets, exception);
            }
        });
    }

    /**
     * Пробуждает потребителя.
     */
    public void wakeup() {
        kafkaConsumer.wakeup();
    }

    /**
     * Закрывает потребителя.
     */
    @PreDestroy
    public void close() {
        log.info("Закрытие Kafka consumer");
        kafkaConsumer.close();
    }
}