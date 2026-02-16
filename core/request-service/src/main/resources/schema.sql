CREATE SCHEMA IF NOT EXISTS request_schema;
SET
search_path TO request_schema;

CREATE TABLE IF NOT EXISTS requests
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    NOT
    NULL,
    created
    TIMESTAMP
    WITHOUT
    TIME
    ZONE
    NOT
    NULL,
    event_id
    BIGINT
    NOT
    NULL,
    requester_id
    BIGINT
    NOT
    NULL,
    status
    VARCHAR
(
    20
) NOT NULL,
    CONSTRAINT pk_request PRIMARY KEY
(
    id
),
    CONSTRAINT uq_request_requester_event UNIQUE
(
    requester_id,
    event_id
)
    );

CREATE INDEX IF NOT EXISTS idx_requests_event ON requests(event_id);
CREATE INDEX IF NOT EXISTS idx_requests_requester ON requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_status ON requests(status);