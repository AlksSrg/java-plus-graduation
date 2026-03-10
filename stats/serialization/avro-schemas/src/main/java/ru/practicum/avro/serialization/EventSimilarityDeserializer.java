package ru.practicum.avro.serialization;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * Десериализатор для EventSimilarityAvro.
 */
public class EventSimilarityDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {

    public EventSimilarityDeserializer() {
        super(EventSimilarityAvro.class, EventSimilarityAvro.getClassSchema());
    }
}