-- Migration: V1__init_result_schema.sql
-- Description: Create initial quiz_attempt and answer tables for Result/Attempt Service

CREATE TABLE IF NOT EXISTS quiz_attempt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL,
    student_email VARCHAR(255) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    submitted_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS answer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES quiz_attempt(id) ON DELETE CASCADE,
    question_id UUID NOT NULL,
    selected_option_id UUID,
    correct BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempt_quiz_id ON quiz_attempt(quiz_id);
CREATE INDEX IF NOT EXISTS idx_quiz_attempt_student_email ON quiz_attempt(student_email);
CREATE INDEX IF NOT EXISTS idx_answer_attempt_id ON answer(attempt_id);
