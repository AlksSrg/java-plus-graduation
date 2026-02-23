CREATE SCHEMA IF NOT EXISTS event_schema;
SET
search_path TO event_schema;

CREATE TABLE IF NOT EXISTS events
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    NOT
    NULL,
    title
    VARCHAR
(
    120
) NOT NULL,
    annotation VARCHAR
(
    2000
) NOT NULL,
    description VARCHAR
(
    7000
) NOT NULL,
    category_id BIGINT NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    initiator_id BIGINT NOT NULL,
    location_lat FLOAT,
    location_lon FLOAT,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    participant_limit INTEGER NOT NULL DEFAULT 0,
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    created_on TIMESTAMP
                         WITHOUT TIME ZONE NOT NULL,
    published_on TIMESTAMP
                         WITHOUT TIME ZONE,
    state VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT pk_event PRIMARY KEY
(
    id
)
    );

CREATE INDEX IF NOT EXISTS idx_events_category ON events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_initiator ON events(initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_state ON events(state);