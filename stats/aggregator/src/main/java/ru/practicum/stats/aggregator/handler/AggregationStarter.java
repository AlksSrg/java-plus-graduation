package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.config.AggregatorConfig;
import ru.practicum.stats.aggregator.kafka.KafkaClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Запускает обработку событий из Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private static final int BATCH_SIZE = 10;

    private final AggregatorConfig config;
    private final KafkaClient kafkaClient;
    private final ObjectMapper objectMapper;
    private final EventSimilarityCollector eventCollector;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private int processedCount;

    /**
     * Запускает цикл обработки сообщений.
     */
    public void start() {
        processedCount = 0;
        Consumer<String, UserActionAvro> consumer = kafkaClient.getConsumer();

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(java.util.List.of(config.getTopics().getUserActions()));
            log.info("Подписка на топик: {}", config.getTopics().getUserActions());

            while (true) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    processRecord(record);
                    saveOffset(record, consumer);
                }
            }

        } catch (WakeupException e) {
            log.info("Завершение работы AggregationStarter");
        } catch (Exception e) {
            log.error("Ошибка обработки", e);
        } finally {
            shutdown(consumer);
        }
    }

    /**
     * Обрабатывает одну запись.
     */
    private void processRecord(ConsumerRecord<String, UserActionAvro> record) {
        eventCollector.updateState(record.value()).forEach(this::sendSimilarity);
    }

    /**
     * Отправляет данные о схожести в Kafka.
     */
    private void sendSimilarity(EventSimilarityAvro similarity) {
        try {
            String json = objectMapper.writeValueAsString(similarity);
            log.debug("Отправка: {}", json);
        } catch (JsonProcessingException e) {
            log.debug("Отправка: {}", similarity);
        }

        ProducerRecord<String, EventSimilarityAvro> record =
                new ProducerRecord<>(config.getTopics().getEventsSimilarity(), similarity);

        kafkaClient.getProducer().send(record, (metadata, ex) -> {
            if (ex != null) {
                log.error("Ошибка отправки", ex);
            } else {
                log.debug("Отправлено в топик {}, partition {}, offset {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    /**
     * Сохраняет смещение для обработки.
     */
    private void saveOffset(ConsumerRecord<String, UserActionAvro> record,
                            Consumer<String, UserActionAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (++processedCount % BATCH_SIZE == 0) {
            consumer.commitAsync(currentOffsets, (offsets, ex) -> {
                if (ex != null) {
                    log.error("Ошибка коммита", ex);
                }
            });
        }
    }

    /**
     * Завершает работу.
     */
    private void shutdown(Consumer<String, UserActionAvro> consumer) {
        try {
            kafkaClient.getProducer().flush();
            consumer.commitSync(currentOffsets);
        } catch (Exception e) {
            log.error("Ошибка при завершении", e);
        } finally {
            kafkaClient.stop();
        }
    }
}