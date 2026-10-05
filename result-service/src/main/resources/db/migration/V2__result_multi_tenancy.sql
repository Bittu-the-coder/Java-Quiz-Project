-- Migration: V2__result_multi_tenancy.sql
-- Description: Add org_id tenant partitioning to quiz_attempt table

ALTER TABLE quiz_attempt ADD COLUMN IF NOT EXISTS org_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000';
ALTER TABLE quiz_attempt ALTER COLUMN org_id DROP DEFAULT;

CREATE INDEX IF NOT EXISTS idx_quiz_attempt_org_id ON quiz_attempt(org_id);
CREATE INDEX IF NOT EXISTS idx_quiz_attempt_org_student ON quiz_attempt(org_id, student_email);
CREATE INDEX IF NOT EXISTS idx_quiz_attempt_org_quiz ON quiz_attempt(org_id, quiz_id);
