package ru.practicum.stats.collector.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

/**
 * Интерфейс Kafka клиента.
 * Предоставляет доступ к продюсеру и управление его жизненным циклом.
 */
public interface KafkaClient {

    /**
     * Возвращает продюсер для отправки сообщений Avro.
     *
     * @return продюсер Kafka
     */
    Producer<Long, SpecificRecordBase> getProducer();

    /**
     * Останавливает продюсера и освобождает ресурсы.
     */
    void stop();
}