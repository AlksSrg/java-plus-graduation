package ru.practicum.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.runner.EventSimilarityProcessor;
import ru.practicum.analyzer.runner.UserActionProcessor;

/**
 * Компонент, запускающий Kafka‑процессоры в отдельных потоках после старта приложения.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorLauncher implements CommandLineRunner {

    private final UserActionProcessor userActionProcessor;
    private final EventSimilarityProcessor eventSimilarityProcessor;

    @Override
    public void run(String... args) {
        log.info("Запуск Kafka процессоров...");

        Thread userThread = new Thread(userActionProcessor, "UserActionProcessor");
        userThread.setDaemon(true);
        userThread.start();

        Thread similarityThread = new Thread(eventSimilarityProcessor, "EventSimilarityProcessor");
        similarityThread.setDaemon(true);
        similarityThread.start();

        log.info("Процессоры запущены");
    }
}