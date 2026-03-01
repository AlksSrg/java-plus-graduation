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

    private final EventSimilarityRepository eventSimilarityRepository;

    /**
     * Обрабатывает и сохраняет данные о схожести.
     *
     * @param avro объект с данными о схожести
     */
    @Transactional
    public void handle(EventSimilarityAvro avro) {
        log.info("Обработка схожести: eventA={}, eventB={}, score={}",
                avro.getEventA(), avro.getEventB(), avro.getScore());

        EventSimilarity similarity = EventSimilarity.builder()
                .eventA(avro.getEventA())
                .eventB(avro.getEventB())
                .score(avro.getScore())
                .timestamp(avro.getTimestamp())
                .build();

        eventSimilarityRepository.save(similarity);
        log.info("Схожесть сохранена");
    }
}