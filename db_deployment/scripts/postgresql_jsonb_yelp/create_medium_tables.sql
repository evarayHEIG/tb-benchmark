-- Delete the schema and all its contents
DROP SCHEMA IF EXISTS yelp_medium CASCADE;

-- Create the schema if it does not exist
CREATE SCHEMA IF NOT EXISTS yelp_medium;

-- Create the temporary table with selected business IDs in the public schema
DROP TABLE IF EXISTS selected_businesses_medium;
CREATE TEMPORARY TABLE selected_businesses_medium AS
SELECT b.data->>'business_id' as business_id
FROM business b
ORDER BY sha256((b.data ->> 'business_id')::bytea)
LIMIT (SELECT ROUND(COUNT(*) * 0.67) FROM business);

-- Create tables in the yelp_medium schema using the selected business IDs
CREATE TABLE yelp_medium.business AS
SELECT * FROM business b
WHERE b.data->>'business_id' IN (SELECT sb.business_id FROM selected_businesses_medium sb);

CREATE TABLE yelp_medium.review AS
SELECT * FROM review r
WHERE r.data->>'business_id' IN (SELECT sb.business_id FROM selected_businesses_medium sb);

CREATE TABLE yelp_medium.checkin AS
SELECT * FROM checkin c
WHERE c.data->>'business_id' IN (SELECT sb.business_id FROM selected_businesses_medium sb);

CREATE TABLE yelp_medium.tip AS
SELECT * FROM tip t
WHERE t.data->>'business_id' IN (SELECT sb.business_id FROM selected_businesses_medium sb);

CREATE TABLE yelp_medium.user AS
SELECT * FROM "user" u
WHERE u.data->>'user_id' IN (SELECT rs.data->>'user_id' FROM yelp_medium.review rs);