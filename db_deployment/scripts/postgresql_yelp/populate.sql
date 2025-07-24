-- Import CSV data to PostgreSQL tables

-- Set client encoding to UTF-8 to handle special characters
SET client_encoding = 'UTF8';

-- Start transaction - this ensures all or nothing approach
BEGIN;

-- Business tables
COPY business FROM '/var/lib/postgresql/imports/business.csv' WITH CSV HEADER NULL '';

COPY business_categories FROM '/var/lib/postgresql/imports/business_categories.csv' WITH CSV HEADER NULL '';

COPY business_hours FROM '/var/lib/postgresql/imports/business_hours.csv' WITH CSV HEADER NULL '';

COPY business_attributes FROM '/var/lib/postgresql/imports/business_attributes.csv' WITH CSV HEADER NULL '';

COPY business_parking FROM '/var/lib/postgresql/imports/business_parking.csv' WITH CSV HEADER NULL '';

COPY best_nights FROM '/var/lib/postgresql/imports/best_nights.csv' WITH CSV HEADER NULL '';

COPY hair_specializes_in FROM '/var/lib/postgresql/imports/hair_specializes_in.csv' WITH CSV HEADER NULL '';

COPY music FROM '/var/lib/postgresql/imports/music.csv' WITH CSV HEADER NULL '';

COPY ambience FROM '/var/lib/postgresql/imports/ambience.csv' WITH CSV HEADER NULL '';

COPY good_for_meal FROM '/var/lib/postgresql/imports/good_for_meal.csv' WITH CSV HEADER NULL '';

COPY dietary_restrictions FROM '/var/lib/postgresql/imports/dietary_restrictions.csv' WITH CSV HEADER NULL '';

-- Checkin tables
COPY checkin FROM '/var/lib/postgresql/imports/checkin.csv' WITH CSV HEADER NULL '';

COPY checkin_date FROM '/var/lib/postgresql/imports/checkin_date.csv' WITH CSV HEADER NULL '';

-- User tables (note the quotes around "user" as it's a reserved word)
COPY "user" FROM '/var/lib/postgresql/imports/user.csv' WITH CSV HEADER NULL '';

COPY user_friends FROM '/var/lib/postgresql/imports/user_friends.csv' WITH CSV HEADER NULL '';

-- Tip table
COPY tip FROM '/var/lib/postgresql/imports/tip.csv' WITH CSV HEADER NULL '';

-- Review table
COPY review FROM '/var/lib/postgresql/imports/review.csv' WITH CSV HEADER NULL '';

-- Commit transaction if everything went well
COMMIT;