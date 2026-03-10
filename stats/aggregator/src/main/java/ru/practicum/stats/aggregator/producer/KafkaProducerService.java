package ru.practicum.stats.aggregator.producer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.aggregator.kafka.KafkaClient;

/**
 * Сервис для отправки сообщений о схожести в Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaClient kafkaClient;

    /**
     * Отправляет сообщение о схожести двух событий.
     *
     * @param similarity объект схожести
     * @param topic      топик для отправки
     */
    public void sendSimilarity(EventSimilarityAvro similarity, String topic) {
        ProducerRecord<String, EventSimilarityAvro> record = new ProducerRecord<>(
                topic,
                null,
                similarity.getTimestamp().toEpochMilli(),
                similarity.getEventA() + "-" + similarity.getEventB(),
                similarity
        );
        kafkaClient.getProducer().send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки сообщения в топик {}: {}", topic, exception.getMessage(), exception);
            } else {
                log.debug("Сообщение отправлено в топик {}, partition {}, offset {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    /**
     * Сбрасывает буфер продюсера.
     */
    public void flush() {
        kafkaClient.getProducer().flush();
    }

    @PreDestroy
    public void close() {
        log.info("Закрытие KafkaProducerService");
        flush();
    }
}