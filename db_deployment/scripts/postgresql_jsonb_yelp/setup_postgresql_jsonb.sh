#!/bin/bash

# Database connection parameters
DB_USER="postgres"
DB_NAME="postgres"
DB_HOST="localhost"  # Change if your database is on a different server
DB_PORT="5432"       # Default PostgreSQL port
DB_PASSWORD="postgres"

# Directory containing SQL scripts
SQL_DIR="."

# Log file for script execution
LOG_FILE="sql_execution_$(date +%Y%m%d_%H%M%S).log"

# Docker compose project name or directory
COMPOSE_DIR="/home/heiguser/docker" 

# Set password as environment variable for psql
export PGPASSWORD="$DB_PASSWORD"

echo "Starting SQL script execution at $(date)" | tee -a "$LOG_FILE"

# Function to run a SQL script
run_script() {
    local script=$1
    echo "Running script: $script" | tee -a "$LOG_FILE"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -a -f "$script" 2>&1 | tee -a "$LOG_FILE"
    local status=${PIPESTATUS[0]}
    if [ $status -eq 0 ]; then
        echo "Script $script executed successfully." | tee -a "$LOG_FILE"
    else
        echo "Script $script failed with status $status." | tee -a "$LOG_FILE"
    fi
    echo "----------------------------------------" | tee -a "$LOG_FILE"
}

# Run specific scripts in a specific order
run_script "$SQL_DIR/schema.sql"
run_script "$SQL_DIR/populate.sql"
run_script "$SQL_DIR/create_small_tables.sql"
run_script "$SQL_DIR/create_medium_tables.sql"
run_script "$SQL_DIR/add_keys.sql"
run_script "$SQL_DIR/index.sql"
run_script "$SQL_DIR/metrics.sql"
run_script "$SQL_DIR/statistics.sql"
run_script "$SQL_DIR/config.sql"

# Unset password environment variable when done
unset PGPASSWORD

echo "Configuration changes applied. Restarting PostgreSQL container..." | tee -a "$LOG_FILE"

cd "$COMPOSE_DIR" && docker compose stop postgres_jsonb 2>&1 | tee -a "$LOG_FILE"
cd "$COMPOSE_DIR" && docker compose start postgres_jsonb 2>&1 | tee -a "$LOG_FILE"

echo "SQL script execution completed at $(date)" | tee -a "$LOG_FILE"