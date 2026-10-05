#!/usr/bin/env bash
set -e

# Automatically create all required databases for the microservices
function create_database() {
    local database=$1
    echo "Creating database '$database' if not exists..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        SELECT 'CREATE DATABASE $database'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

create_database "auth_db"
create_database "quiz_db"
create_database "question_db"
create_database "result_db"
create_database "attempt_db"
create_database "grading_db"

echo "All Assessify microservice databases initialized successfully."
