package ru.practicum.analyzer.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.consumer.EventSimilarityConsumerService;
import ru.practicum.analyzer.handlers.EventSimilarityHandler;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.List;

/**
 * Процессор, непрерывно читающий топик схожести мероприятий и передающий их обработчику.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {

    private final EventSimilarityConsumerService consumer;
    private final EventSimilarityHandler handler;

    @Value("${kafka.topics.event-similarity:telemetry.similarity.v1}")
    private String topic;

    private volatile boolean running = true;

    @Override
    public void run() {
        setupShutdownHook();
        consumer.subscribe(List.of(topic));
        log.info("Запуск процессора схожести мероприятий, топик: {}", topic);

        try {
            while (running) {
                ConsumerRecords<Long, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(5000));

                if (!records.isEmpty()) {
                    log.info("Получено {} записей", records.count());
                    for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                        try {
                            EventSimilarityAvro avro = (EventSimilarityAvro) record.value();
                            handler.handle(avro);
                        } catch (Exception e) {
                            log.error("Ошибка обработки записи offset={}", record.offset(), e);
                        }
                    }
                    consumer.commitAsync();
                }
            }
        } catch (WakeupException e) {
            log.info("WakeupException при остановке процессора схожести");
        } catch (Exception e) {
            log.error("Ошибка в процессоре схожести", e);
        } finally {
            consumer.close();
        }
    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            consumer.wakeup();
        }));
    }
}