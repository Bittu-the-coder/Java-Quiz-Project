-- Migration: V3__question_bank_enrichment.sql
-- Description: Add difficulty, category, tags, and marks to Question Bank

ALTER TABLE question ADD COLUMN IF NOT EXISTS difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE question ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE question ADD COLUMN IF NOT EXISTS tags VARCHAR(255);
ALTER TABLE question ADD COLUMN IF NOT EXISTS marks INT NOT NULL DEFAULT 1;
ALTER TABLE question ADD COLUMN IF NOT EXISTS negative_marks DOUBLE PRECISION NOT NULL DEFAULT 0.00;

CREATE INDEX IF NOT EXISTS idx_question_category ON question(org_id, category);
CREATE INDEX IF NOT EXISTS idx_question_difficulty ON question(org_id, difficulty);
