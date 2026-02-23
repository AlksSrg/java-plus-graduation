-- Создание схемы
CREATE SCHEMA IF NOT EXISTS user_schema;
SET search_path TO user_schema;

-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    NOT
    NULL,
    name
    VARCHAR
(
    250
) NOT NULL,
    email VARCHAR
(
    254
) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY
(
    id
),
    CONSTRAINT uq_user_email UNIQUE
(
    email
)
    );