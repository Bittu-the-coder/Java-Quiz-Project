-- Migration: V3__attempt_state_machine.sql
-- Description: Add attempt state machine, server-authoritative timer, and autosave constraints

ALTER TABLE quiz_attempt ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS';
ALTER TABLE quiz_attempt ADD COLUMN IF NOT EXISTS started_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE quiz_attempt ADD COLUMN IF NOT EXISTS server_deadline TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE quiz_attempt ADD COLUMN IF NOT EXISTS duration_minutes INT NOT NULL DEFAULT 60;

ALTER TABLE answer ADD COLUMN IF NOT EXISTS answered_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Constraint ensuring upsert semantics for per-question autosaves
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_attempt_question'
    ) THEN
        ALTER TABLE answer ADD CONSTRAINT uq_attempt_question UNIQUE (attempt_id, question_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_quiz_attempt_status ON quiz_attempt(status);
CREATE INDEX IF NOT EXISTS idx_quiz_attempt_deadline ON quiz_attempt(server_deadline);
