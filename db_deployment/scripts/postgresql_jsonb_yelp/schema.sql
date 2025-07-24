-- Drop the table if it exists
DROP TABLE IF EXISTS business;
DROP TABLE IF EXISTS "user";
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS checkin;
DROP TABLE IF EXISTS tip;

-- Creation of a table that uses JSONB type
CREATE TABLE public.business
(
    id   VARCHAR(255) PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE public."user"
(
    id   VARCHAR(255) PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE public.review
(
    id   VARCHAR(255) PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE public.checkin
(
    id   SERIAL PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE public.tip
(
    id   SERIAL PRIMARY KEY,
    data JSONB NOT NULL
);

-- Grant all privileges to the user
GRANT ALL PRIVILEGES ON DATABASE postgres to postgres;