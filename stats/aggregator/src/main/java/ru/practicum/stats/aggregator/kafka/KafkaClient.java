package ru.practicum.stats.aggregator.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Интерфейс Kafka клиента.
 * Предоставляет доступ к продюсеру и консьюмеру.
 */
public interface KafkaClient {

    /**
     * Возвращает продюсер для отправки сообщений о схожести.
     *
     * @return продюсер
     */
    Producer<String, EventSimilarityAvro> getProducer();

    /**
     * Возвращает консьюмер для чтения действий пользователей.
     *
     * @return консьюмер
     */
    Consumer<String, UserActionAvro> getConsumer();

    /**
     * Останавливает клиент и закрывает все ресурсы.
     */
    void stop();
}