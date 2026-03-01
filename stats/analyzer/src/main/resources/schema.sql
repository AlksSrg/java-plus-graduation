-- Создать схему, если не существует
CREATE SCHEMA IF NOT EXISTS analyzer_schema;

-- Переключиться на схему
SET search_path TO analyzer_schema;

-- Таблица для хранения действий пользователей
CREATE TABLE IF NOT EXISTS users_actions (
                                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             event_id BIGINT NOT NULL,
                                             action_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
                            CONSTRAINT uk_user_event UNIQUE (user_id, event_id)
    );

-- Таблица для хранения схожести мероприятий
CREATE TABLE IF NOT EXISTS events_similarity (
                                                 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                 event_a BIGINT NOT NULL,
                                                 event_b BIGINT NOT NULL,
                                                 score DOUBLE PRECISION NOT NULL,
                                                 timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
                                                 CONSTRAINT uk_event_pair UNIQUE (event_a, event_b)
    );

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_users_actions_user_id ON users_actions(user_id);
CREATE INDEX IF NOT EXISTS idx_users_actions_event_id ON users_actions(event_id);
CREATE INDEX IF NOT EXISTS idx_events_similarity_event_a ON events_similarity(event_a);
CREATE INDEX IF NOT EXISTS idx_events_similarity_event_b ON events_similarity(event_b);
CREATE INDEX IF NOT EXISTS idx_events_similarity_score ON events_similarity(score DESC);