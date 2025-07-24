-- Pour la table business
DROP TABLE IF EXISTS temp_business;
CREATE TEMP TABLE temp_business(data JSONB);
COPY temp_business(data) FROM '/var/lib/postgresql/imports/business_csv_jsonb.json' CSV QUOTE '"';
INSERT INTO business(id, data)
SELECT (data->>'business_id'), data FROM temp_business;
DROP TABLE temp_business;

-- Pour la table user
DROP TABLE IF EXISTS temp_user;
CREATE TEMP TABLE temp_user(data JSONB);
COPY temp_user(data) FROM '/var/lib/postgresql/imports/user_csv_jsonb.json' CSV QUOTE '"';
INSERT INTO "user"(id, data)
SELECT (data->>'user_id'), data FROM temp_user;
DROP TABLE temp_user;

-- Pour la table review
DROP TABLE IF EXISTS temp_review;
CREATE TEMP TABLE temp_review(data JSONB);
COPY temp_review(data) FROM '/var/lib/postgresql/imports/review_csv_jsonb.json' CSV QUOTE '"';
INSERT INTO review(id, data)
SELECT (data->>'review_id'), data FROM temp_review;
DROP TABLE temp_review;

COPY checkin(data)
    FROM '/var/lib/postgresql/imports/checkin_csv_jsonb.json' CSV QUOTE '"';

COPY tip(data)
    FROM '/var/lib/postgresql/imports/tip_csv_jsonb.json' CSV QUOTE '"';

