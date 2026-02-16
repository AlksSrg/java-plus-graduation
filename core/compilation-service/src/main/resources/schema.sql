-- Создание схемы
CREATE SCHEMA IF NOT EXISTS compilation_schema;
SET search_path TO compilation_schema;

-- Создание таблицы подборок
CREATE TABLE IF NOT EXISTS compilations
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    title
    VARCHAR
(
    120
) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE
    );

-- Создание таблицы связи подборок и событий
CREATE TABLE IF NOT EXISTS compilation_events
(
    compilation_id
    BIGINT
    NOT
    NULL,
    event_id
    BIGINT
    NOT
    NULL,
    PRIMARY
    KEY
(
    compilation_id,
    event_id
),
    CONSTRAINT fk_compilation_events_compilation FOREIGN KEY
(
    compilation_id
) REFERENCES compilations
(
    id
) ON DELETE CASCADE
    );

-- Создание индексов для оптимизации
CREATE INDEX IF NOT EXISTS idx_compilations_pinned ON compilations(pinned);
CREATE INDEX IF NOT EXISTS idx_compilation_events_event_id ON compilation_events(event_id);