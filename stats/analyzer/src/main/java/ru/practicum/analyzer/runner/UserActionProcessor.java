package ru.practicum.analyzer.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.consumer.UserActionConsumerService;
import ru.practicum.analyzer.handlers.UserActionHandler;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

/**
 * Процессор для обработки сообщений о действиях пользователей.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {

    private final UserActionConsumerService consumer;
    private final UserActionHandler userActionHandler;

    @Value("${kafka.topics.user-actions:telemetry.actions.v1}")
    private String topic;

    private volatile boolean running = true;

    /**
     * Запускает процессор.
     */
    @Override
    public void run() {
        setupShutdownHook();
        consumer.subscribe(List.of(topic));
        log.info("Запуск процессора действий, топик: {}", topic);

        try {
            while (running) {
                ConsumerRecords<Long, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(5000));

                if (!records.isEmpty()) {
                    log.info("Получено {} записей", records.count());

                    for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                        try {
                            UserActionAvro avro = (UserActionAvro) record.value();
                            userActionHandler.handle(avro);
                        } catch (Exception e) {
                            log.error("Ошибка обработки записи offset={}", record.offset(), e);
                        }
                    }

                    consumer.commitAsync();
                }
            }
        } catch (WakeupException e) {
            log.info("WakeupException при остановке");
        } catch (Exception e) {
            log.error("Ошибка в процессоре", e);
        } finally {
            consumer.close();
        }
    }

    /**
     * Настраивает хук завершения.
     */
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            consumer.wakeup();
        }));
    }
}