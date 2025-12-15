
-- DELETE FROM quiz_attempt_answer;
-- DELETE FROM quiz_attempt;
-- DELETE FROM option;
-- DELETE FROM question;
-- DELETE FROM quiz;
-- DELETE FROM users;











-- INSERT INTO users (name, email, password, provider, role)
-- VALUES (
--     'Admin User',
--     'admin@quizapp.com',
--     '$2a$10$EXAMPLE_HASHED_PASSWORD',
--     'LOCAL',
--     'ADMIN'
-- );


-- CREATE OR REPLACE FUNCTION update_timestamp()
-- RETURNS TRIGGER AS $$
-- BEGIN
--     NEW.updated_at = CURRENT_TIMESTAMP;
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;

-- CREATE TRIGGER update_users_timestamp
-- BEFORE UPDATE ON users
-- FOR EACH ROW
-- EXECUTE FUNCTION update_timestamp();



-- -- CREATE TABLE question (
-- --     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

-- --     quiz_id UUID NOT NULL,

-- --     question_text TEXT NOT NULL,

-- --     question_type VARCHAR(20) DEFAULT 'MCQ',

-- --     marks INT DEFAULT 1,

-- --     CONSTRAINT fk_question_quiz
-- --         FOREIGN KEY (quiz_id)
-- --         REFERENCES quiz(id)
-- --         ON DELETE CASCADE
-- -- );


-- -- CREATE TABLE option (
-- --     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

-- --     question_id UUID NOT NULL,

-- --     option_text TEXT NOT NULL,

-- --     is_correct BOOLEAN DEFAULT FALSE,

-- --     CONSTRAINT fk_option_question
-- --         FOREIGN KEY (question_id)
-- --         REFERENCES question(id)
-- --         ON DELETE CASCADE
-- -- );



-- -- CREATE TABLE quiz_attempt (
-- --     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

-- --     quiz_id UUID NOT NULL,

-- --     student_id UUID NOT NULL,

-- --     score INT DEFAULT 0,

-- --     status VARCHAR(20) NOT NULL CHECK (status IN ('STARTED', 'SUBMITTED')),

-- --     started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- --     submitted_at TIMESTAMP,

-- --     CONSTRAINT fk_attempt_quiz
-- --         FOREIGN KEY (quiz_id)
-- --         REFERENCES quiz(id)
-- --         ON DELETE CASCADE,

-- --     CONSTRAINT fk_attempt_student
-- --         FOREIGN KEY (student_id)
-- --         REFERENCES users(id)
-- --         ON DELETE CASCADE,

-- --     CONSTRAINT uq_student_quiz UNIQUE (quiz_id, student_id)
-- -- );



-- -- CREATE TABLE quiz_attempt_answer (
-- --     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

-- --     quiz_attempt_id UUID NOT NULL,

-- --     question_id UUID NOT NULL,

-- --     selected_option_id UUID NOT NULL,

-- --     is_correct BOOLEAN,

-- --     CONSTRAINT fk_answer_attempt
-- --         FOREIGN KEY (quiz_attempt_id)
-- --         REFERENCES quiz_attempt(id)
-- --         ON DELETE CASCADE,

-- --     CONSTRAINT fk_answer_question
-- --         FOREIGN KEY (question_id)
-- --         REFERENCES question(id)
-- --         ON DELETE CASCADE,

-- --     CONSTRAINT fk_answer_option
-- --         FOREIGN KEY (selected_option_id)
-- --         REFERENCES option(id)
-- --         ON DELETE CASCADE,

-- --     CONSTRAINT uq_attempt_question UNIQUE (quiz_attempt_id, question_id)
-- -- );



-- -- CREATE INDEX idx_user_email ON users(email);
-- -- CREATE INDEX idx_quiz_active ON quiz(is_active);
-- -- CREATE INDEX idx_question_quiz ON question(quiz_id);
-- -- CREATE INDEX idx_attempt_student ON quiz_attempt(student_id);



-- -- CREATE TABLE quiz (
-- --     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

-- --     title VARCHAR(150) NOT NULL,

-- --     description TEXT,

-- --     created_by UUID NOT NULL,

-- --     is_active BOOLEAN DEFAULT FALSE,

-- --     total_marks INT DEFAULT 0,

-- --     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- --     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- --     CONSTRAINT fk_quiz_creator
-- --         FOREIGN KEY (created_by)
-- --         REFERENCES users(id)
-- --         ON DELETE CASCADE
-- -- );



-- -- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -- CREATE TABLE users (
-- --     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

-- --     name VARCHAR(100) NOT NULL,

-- --     email VARCHAR(150) UNIQUE NOT NULL,

-- --     password VARCHAR(255),

-- --     provider VARCHAR(20) NOT NULL CHECK (provider IN ('LOCAL', 'GOOGLE')),

-- --     role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STUDENT')),

-- --     is_active BOOLEAN DEFAULT TRUE,

-- --     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- --     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- -- );

