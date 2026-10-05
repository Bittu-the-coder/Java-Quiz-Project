-- Migration: V1__init_question_schema.sql
-- Description: Create initial question and option tables for Question Bank Service

CREATE TABLE IF NOT EXISTS question (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL,
    text TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS option (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_question_quiz_id ON question(quiz_id);
CREATE INDEX IF NOT EXISTS idx_option_question_id ON option(question_id);
