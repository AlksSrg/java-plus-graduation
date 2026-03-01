package ru.practicum.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.runner.EventSimilarityProcessor;
import ru.practicum.analyzer.runner.UserActionProcessor;

/**
 * Запускает обработчики Kafka сообщений.
 */
@Slf4j
@Component
public class ProcessorLauncher implements CommandLineRunner {

    private final UserActionProcessor userActionProcessor;
    private final EventSimilarityProcessor eventSimilarityProcessor;

    /**
     * Конструктор.
     *
     * @param userActionProcessor      процессор действий
     * @param eventSimilarityProcessor процессор схожести
     */
    public ProcessorLauncher(UserActionProcessor userActionProcessor,
                             EventSimilarityProcessor eventSimilarityProcessor) {
        this.userActionProcessor = userActionProcessor;
        this.eventSimilarityProcessor = eventSimilarityProcessor;
    }

    /**
     * Запускает процессоры в отдельных потоках.
     *
     * @param args аргументы командной строки
     */
    @Override
    public void run(String... args) {
        log.info("Запуск Kafka процессоров...");

        Thread userThread = new Thread(userActionProcessor, "UserActionProcessor");
        userThread.setDaemon(false);
        userThread.start();

        Thread similarityThread = new Thread(eventSimilarityProcessor, "EventSimilarityProcessor");
        similarityThread.setDaemon(false);
        similarityThread.start();

        log.info("Процессоры запущены");
    }
}