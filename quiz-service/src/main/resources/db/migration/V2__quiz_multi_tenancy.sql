-- Migration: V2__quiz_multi_tenancy.sql
-- Description: Add org_id tenant partitioning to quizzes table

ALTER TABLE quizzes ADD COLUMN IF NOT EXISTS org_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000';
ALTER TABLE quizzes ALTER COLUMN org_id DROP DEFAULT;

CREATE INDEX IF NOT EXISTS idx_quizzes_org_id ON quizzes(org_id);
