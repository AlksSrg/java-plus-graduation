package ru.practicum.stats.aggregator.runner;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.config.AggregatorConfig;
import ru.practicum.stats.aggregator.kafka.KafkaClient;
import ru.practicum.stats.aggregator.producer.KafkaProducerService;
import ru.practicum.stats.aggregator.service.AggregationService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Компонент, запускающий цикл потребления сообщений из Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorRunner {

    private static final int BATCH_SIZE = 10;

    private final AggregatorConfig config;
    private final KafkaClient kafkaClient;
    private final KafkaProducerService producerService;
    private final AggregationService aggregationService;

    private volatile boolean running = true;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private int processedCount = 0;

    @PostConstruct
    public void start() {
        Thread thread = new Thread(this::run);
        thread.setDaemon(true);
        thread.start();
    }

    private void run() {
        Consumer<String, UserActionAvro> consumer = kafkaClient.getConsumer();
        consumer.subscribe(List.of(config.getTopics().getUserActions()));
        log.info("Подписка на топик: {}", config.getTopics().getUserActions());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения, вызываем consumer.wakeup()");
            running = false;
            consumer.wakeup();
        }));

        try {
            while (running) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    processRecord(record);
                    saveOffset(record);
                }
                // Асинхронный коммит при накоплении пачки
                if (processedCount >= BATCH_SIZE) {
                    consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                        if (exception != null) {
                            log.error("Ошибка асинхронного коммита", exception);
                        }
                    });
                    processedCount = 0;
                }
            }
        } catch (WakeupException e) {
            if (running) {
                log.error("Неожиданный WakeupException", e);
            } else {
                log.info("WakeupException получен при остановке");
            }
        } catch (Exception e) {
            log.error("Ошибка в цикле потребления", e);
        } finally {
            shutdown(consumer);
        }
    }

    private void processRecord(ConsumerRecord<String, UserActionAvro> record) {
        UserActionAvro action = record.value();
        log.debug("Обработка действия: {}", action);
        List<EventSimilarityAvro> similarities = aggregationService.updateSimilarity(action);
        for (EventSimilarityAvro similarity : similarities) {
            producerService.sendSimilarity(similarity, config.getTopics().getEventsSimilarity());
        }
        log.debug("Действие обработано, сгенерировано {} схожестей", similarities.size());
    }

    private void saveOffset(ConsumerRecord<String, UserActionAvro> record) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
        processedCount++;
    }

    private void shutdown(Consumer<String, UserActionAvro> consumer) {
        log.info("Завершение работы AggregatorRunner");
        try {
            producerService.flush();
            consumer.commitSync(currentOffsets);
            log.info("Смещения синхронно зафиксированы");
        } catch (Exception e) {
            log.error("Ошибка при финальном коммите", e);
        } finally {
            consumer.close();
            kafkaClient.stop();
            log.info("Ресурсы Kafka закрыты");
        }
    }
}