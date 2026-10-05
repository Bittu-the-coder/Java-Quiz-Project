-- Migration: V2__question_multi_tenancy.sql
-- Description: Add org_id tenant partitioning to question table

ALTER TABLE question ADD COLUMN IF NOT EXISTS org_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000';
ALTER TABLE question ALTER COLUMN org_id DROP DEFAULT;

CREATE INDEX IF NOT EXISTS idx_question_org_id ON question(org_id);
CREATE INDEX IF NOT EXISTS idx_question_org_quiz ON question(org_id, quiz_id);
