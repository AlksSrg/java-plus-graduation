package ru.practicum.analyzer.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * Обработчик данных о схожести мероприятий.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityHandler {

    private final EventSimilarityRepository repository;

    /**
     * Сохраняет информацию о схожести двух мероприятий.
     *
     * @param avro объект схожести в формате Avro
     */
    @Transactional
    public void handle(EventSimilarityAvro avro) {
        log.info("Сохранение схожести: eventA={}, eventB={}, score={}",
                avro.getEventA(), avro.getEventB(), avro.getScore());

        EventSimilarity similarity = EventSimilarity.builder()
                .eventA(avro.getEventA())
                .eventB(avro.getEventB())
                .score(avro.getScore())
                .timestamp(avro.getTimestamp())
                .build();

        repository.save(similarity);
        log.info("Схожесть сохранена");
    }
}