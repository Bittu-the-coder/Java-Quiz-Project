-- Migration: V5__grading_and_analytics.sql
-- Description: Create exam_result and item_analysis tables for psychometrics and cohort ranking

CREATE TABLE IF NOT EXISTS exam_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    quiz_id UUID NOT NULL,
    attempt_id UUID UNIQUE NOT NULL REFERENCES quiz_attempt(id) ON DELETE CASCADE,
    student_email VARCHAR(255) NOT NULL,
    total_score DOUBLE PRECISION NOT NULL,
    max_possible_score DOUBLE PRECISION NOT NULL,
    percentage DOUBLE PRECISION NOT NULL,
    rank_in_exam INT,
    percentile DOUBLE PRECISION,
    passed BOOLEAN NOT NULL DEFAULT FALSE,
    graded_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS item_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    quiz_id UUID NOT NULL,
    question_id UUID NOT NULL,
    total_attempts INT NOT NULL DEFAULT 0,
    correct_attempts INT NOT NULL DEFAULT 0,
    difficulty_index DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    discrimination_idx DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    CONSTRAINT uq_quiz_question_item UNIQUE (quiz_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_result_quiz ON exam_result(org_id, quiz_id);
CREATE INDEX IF NOT EXISTS idx_exam_result_attempt ON exam_result(attempt_id);
CREATE INDEX IF NOT EXISTS idx_item_analysis_quiz ON item_analysis(org_id, quiz_id);
