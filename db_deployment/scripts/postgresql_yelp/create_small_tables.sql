-- Delete schema and its contents if it exists
DROP SCHEMA IF EXISTS yelp_small CASCADE;

-- Create the schema if it does not exist
CREATE SCHEMA IF NOT EXISTS yelp_small;

-- Create the temporary table in the public schema
DROP TABLE IF EXISTS selected_businesses;
CREATE TEMPORARY TABLE selected_businesses AS
SELECT business_id
FROM business b
order by sha256(b.business_id::bytea)
LIMIT (SELECT COUNT(*) * 0.33 FROM business);

-- Create tables in the yelp_small schema using the selected business IDs
CREATE TABLE yelp_small.business AS
SELECT * FROM business b
WHERE b.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.review AS
SELECT * FROM review r
WHERE r.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.checkin AS
SELECT * FROM checkin c
WHERE c.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.tip AS
SELECT * FROM tip t
WHERE t.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.user AS
SELECT * FROM "user" u
WHERE u.user_id IN (SELECT rs.user_id FROM yelp_small.review rs);

CREATE TABLE yelp_small.ambience AS
SELECT * FROM ambience a
WHERE a.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.best_nights AS
SELECT * FROM best_nights bn
WHERE bn.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.business_attributes AS
SELECT * FROM business_attributes ba
WHERE ba.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.business_categories AS
SELECT * FROM business_categories bc
WHERE bc.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.business_hours AS
SELECT * FROM business_hours bh
WHERE bh.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.business_parking AS
SELECT * FROM business_parking bp
WHERE bp.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.dietary_restrictions AS
SELECT * FROM dietary_restrictions dr
WHERE dr.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.good_for_meal AS
SELECT * FROM good_for_meal g
WHERE g.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.hair_specializes_in AS
SELECT * FROM hair_specializes_in h
WHERE h.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.music AS
SELECT * FROM music m
WHERE m.business_id IN (SELECT sb.business_id FROM selected_businesses sb);

CREATE TABLE yelp_small.user_friends AS
SELECT * FROM user_friends uf
WHERE uf.user_id IN (SELECT u.user_id FROM yelp_small.user u);

CREATE TABLE yelp_small.checkin_date AS
SELECT * FROM checkin_date cd
WHERE cd.checkin_id IN (SELECT c.checkin_id FROM yelp_small.checkin c);