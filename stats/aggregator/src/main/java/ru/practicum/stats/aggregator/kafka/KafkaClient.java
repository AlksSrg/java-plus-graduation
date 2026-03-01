package ru.practicum.stats.aggregator.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Интерфейс Kafka клиента.
 */
public interface KafkaClient {

    /**
     * Возвращает продюсер для отправки схожести.
     */
    Producer<String, EventSimilarityAvro> getProducer();

    /**
     * Возвращает консьюмер для чтения действий.
     */
    Consumer<String, UserActionAvro> getConsumer();

    /**
     * Останавливает клиент.
     */
    void stop();
}