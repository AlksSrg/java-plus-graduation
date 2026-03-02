package ru.practicum.avro.serialization;

import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Десериализатор для UserActionAvro.
 */
public class UserActionDeserializer extends BaseAvroDeserializer<UserActionAvro> {

    public UserActionDeserializer() {
        super(UserActionAvro.class, UserActionAvro.getClassSchema());
    }
}