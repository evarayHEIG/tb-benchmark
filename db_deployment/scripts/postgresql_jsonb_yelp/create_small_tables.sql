-- Delete schema and all its contents
DROP SCHEMA IF EXISTS yelp_small CASCADE;

-- Create the schema if it does not exist
CREATE SCHEMA IF NOT EXISTS yelp_small;

-- Create the temporary table with selected business IDs in the public schema
DROP TABLE IF EXISTS selected_businesses;
CREATE TEMPORARY TABLE selected_businesses AS
SELECT b.data ->> 'business_id' as business_id
FROM business b
ORDER BY sha256((b.data ->> 'business_id')::bytea)
LIMIT (SELECT ROUND(COUNT(*) * 0.33) FROM business);

-- Create tables in the yelp_small schema using the selected business IDs
CREATE TABLE yelp_small.business AS
SELECT *
FROM business b
WHERE b.data ->> 'business_id' IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.review AS
SELECT *
FROM review r
WHERE r.data ->> 'business_id' IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.checkin AS
SELECT *
FROM checkin c
WHERE c.data ->> 'business_id' IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.tip AS
SELECT *
FROM tip t
WHERE t.data ->> 'business_id' IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.user AS
SELECT *
FROM "user" u
WHERE u.data ->> 'user_id' IN (SELECT rs.data ->> 'user_id' FROM yelp_small.review rs);