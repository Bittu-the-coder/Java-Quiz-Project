-- Migration: V4__proctor_events.sql
-- Description: Create immutable proctor event audit trail with sequence enforcement

CREATE TABLE IF NOT EXISTS proctor_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    attempt_id UUID NOT NULL REFERENCES quiz_attempt(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    sequence_num INT NOT NULL,
    occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recorded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    signature VARCHAR(128),
    details TEXT,
    CONSTRAINT uq_attempt_sequence UNIQUE (attempt_id, sequence_num)
);

CREATE INDEX IF NOT EXISTS idx_proctor_attempt ON proctor_event(attempt_id);
CREATE INDEX IF NOT EXISTS idx_proctor_org ON proctor_event(org_id);
