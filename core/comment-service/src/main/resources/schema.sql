-- Создание схемы
CREATE SCHEMA IF NOT EXISTS comment_schema;
SET search_path TO comment_schema;

-- Таблица для хранения комментариев
CREATE TABLE IF NOT EXISTS comments
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    author_id
    BIGINT
    NOT
    NULL,
    event_id
    BIGINT
    NOT
    NULL,
    created
    TIMESTAMP
    WITHOUT
    TIME
    ZONE
    NOT
    NULL,
    text
    VARCHAR
(
    5000
) NOT NULL
    );

-- Создание индексов для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);
CREATE INDEX IF NOT EXISTS idx_comments_event_id ON comments(event_id);
CREATE INDEX IF NOT EXISTS idx_comments_created ON comments(created);