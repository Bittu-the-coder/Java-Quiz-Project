-- Migration: V3__quiz_invitations.sql
-- Description: Add quiz invitations for bulk candidate registration

CREATE TABLE IF NOT EXISTS quiz_invitation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    candidate_email VARCHAR(255) NOT NULL,
    token VARCHAR(64) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invited_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uq_quiz_candidate UNIQUE (quiz_id, candidate_email)
);

CREATE INDEX IF NOT EXISTS idx_invitation_org_id ON quiz_invitation(org_id);
CREATE INDEX IF NOT EXISTS idx_invitation_quiz_id ON quiz_invitation(quiz_id);
CREATE INDEX IF NOT EXISTS idx_invitation_token ON quiz_invitation(token);
CREATE INDEX IF NOT EXISTS idx_invitation_email ON quiz_invitation(candidate_email);
