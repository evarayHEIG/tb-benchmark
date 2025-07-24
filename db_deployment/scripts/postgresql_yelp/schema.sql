-- SCHEMA CREATION FOR YELP UML (NO ENUMS, STRING COLUMNS INSTEAD)

-- DROP TABLES IN CORRECT ORDER (to avoid FK issues)
DROP TABLE IF EXISTS user_friends CASCADE;
DROP TABLE IF EXISTS tip CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS checkin_date CASCADE;
DROP TABLE IF EXISTS checkin CASCADE;
DROP TABLE IF EXISTS music CASCADE;
DROP TABLE IF EXISTS hair_specializes_in CASCADE;
DROP TABLE IF EXISTS good_for_meal CASCADE;
DROP TABLE IF EXISTS dietary_restrictions CASCADE;
DROP TABLE IF EXISTS business_parking CASCADE;
DROP TABLE IF EXISTS best_nights CASCADE;
DROP TABLE IF EXISTS ambience CASCADE;
DROP TABLE IF EXISTS business_attributes CASCADE;
DROP TABLE IF EXISTS business_hours CASCADE;
DROP TABLE IF EXISTS business_categories CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS business CASCADE;

-- BUSINESS MAIN TABLE
CREATE TABLE IF NOT EXISTS business
(
    business_id  VARCHAR PRIMARY KEY,
    name         VARCHAR NOT NULL,
    address      VARCHAR,
    city         VARCHAR,
    state        VARCHAR,
    postal_code  VARCHAR,
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    stars        DOUBLE PRECISION,
    review_count INT,
    is_open      INT
);

-- BUSINESS CATEGORIES TABLE TO REPRESENT MUTLIVALUED ATTRIBUTE
CREATE TABLE IF NOT EXISTS business_categories
(
    business_id VARCHAR REFERENCES business (business_id) ON DELETE CASCADE,
    category    VARCHAR,
    PRIMARY KEY (business_id, category)
);

-- BUSINESS HOURS TABLE
CREATE TABLE IF NOT EXISTS business_hours
(
    business_id VARCHAR REFERENCES business (business_id) ON DELETE CASCADE,
    day         VARCHAR,
    open_time   TIME,
    close_time  TIME,
    PRIMARY KEY (business_id, day)
);

-- BUSINESS ATTRIBUTES TABLE (NO ENUMS - REPLACED BY VARCHAR)
CREATE TABLE IF NOT EXISTS business_attributes
(
    business_id                   VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    accepts_insurance             BOOLEAN,
    byob                          BOOLEAN,
    bike_parking                  BOOLEAN,
    business_accepts_bitcoin      BOOLEAN,
    business_accepts_credit_cards BOOLEAN,
    by_appointment_only           BOOLEAN,
    caters                        BOOLEAN,
    coat_check                    BOOLEAN,
    corkage                       BOOLEAN,
    dogs_allowed                  BOOLEAN,
    drive_thru                    BOOLEAN,
    good_for_dancing              BOOLEAN,
    good_for_kids                 BOOLEAN,
    happy_hour                    BOOLEAN,
    has_tv                        BOOLEAN,
    noise_level                   VARCHAR,
    open_24_hours                 BOOLEAN,
    outdoor_seating               BOOLEAN,
    restaurants_counter_service   BOOLEAN,
    restaurants_delivery          BOOLEAN,
    restaurants_good_for_groups   BOOLEAN,
    restaurants_price_range2      INT,
    restaurants_reservations      BOOLEAN,
    restaurants_table_service     BOOLEAN,
    restaurants_take_out          BOOLEAN,
    wheelchair_accessible         BOOLEAN,
    smoking                       VARCHAR,
    wifi                          VARCHAR,
    restaurants_attire            VARCHAR,
    byob_corkage                  VARCHAR,
    alcohol                       VARCHAR,
    ages_allowed                  VARCHAR
);

-- AMBIENCE TABLE
CREATE TABLE IF NOT EXISTS ambience
(
    business_id VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    touristy    BOOLEAN,
    hipster     BOOLEAN,
    romantic    BOOLEAN,
    divey       BOOLEAN,
    intimate    BOOLEAN,
    trendy      BOOLEAN,
    upscale     BOOLEAN,
    classy      BOOLEAN,
    casual      BOOLEAN
);

-- BEST NIGHTS TABLE
CREATE TABLE IF NOT EXISTS best_nights
(
    business_id VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    monday      BOOLEAN,
    tuesday     BOOLEAN,
    wednesday   BOOLEAN,
    thursday    BOOLEAN,
    friday      BOOLEAN,
    saturday    BOOLEAN,
    sunday      BOOLEAN
);

-- BUSINESS PARKING TABLE
CREATE TABLE IF NOT EXISTS business_parking
(
    business_id VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    garage      BOOLEAN,
    street      BOOLEAN,
    validated   BOOLEAN,
    lot         BOOLEAN,
    valet       BOOLEAN
);

-- DIETARY RESTRICTIONS TABLE
CREATE TABLE IF NOT EXISTS dietary_restrictions
(
    business_id VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    dairy_free  BOOLEAN,
    gluten_free BOOLEAN,
    vegan       BOOLEAN,
    kosher      BOOLEAN,
    halal       BOOLEAN,
    soy_free    BOOLEAN,
    vegetarian  BOOLEAN
);

-- GOOD FOR MEAL TABLE
CREATE TABLE IF NOT EXISTS good_for_meal
(
    business_id VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    dessert     BOOLEAN,
    latenight   BOOLEAN,
    lunch       BOOLEAN,
    dinner      BOOLEAN,
    brunch      BOOLEAN,
    breakfast   BOOLEAN
);

-- HAIR SPECIALIZES IN TABLE
CREATE TABLE IF NOT EXISTS hair_specializes_in
(
    business_id     VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    straightperms   BOOLEAN,
    coloring        BOOLEAN,
    extensions      BOOLEAN,
    africanamerican BOOLEAN,
    curly           BOOLEAN,
    kids            BOOLEAN,
    perms           BOOLEAN,
    asian           BOOLEAN
);

-- MUSIC TABLE
CREATE TABLE IF NOT EXISTS music
(
    business_id      VARCHAR PRIMARY KEY REFERENCES business (business_id) ON DELETE CASCADE,
    dj               BOOLEAN,
    background_music BOOLEAN,
    no_music         BOOLEAN,
    jukebox          BOOLEAN,
    live             BOOLEAN,
    video            BOOLEAN,
    karaoke          BOOLEAN
);

-- CHECKIN TABLE
CREATE TABLE IF NOT EXISTS checkin
(
    checkin_id  SERIAL PRIMARY KEY,
    business_id VARCHAR NOT NULL REFERENCES business (business_id) ON DELETE CASCADE
);

-- CHECKIN DATE TABLE TO REPRESENT MUTLIVALUED ATTRIBUTE
CREATE TABLE IF NOT EXISTS checkin_date
(
    checkin_id SERIAL REFERENCES checkin (checkin_id) ON DELETE CASCADE,
    date       TIMESTAMP,
    PRIMARY KEY (checkin_id, date)
);

-- USER TABLE
CREATE TABLE IF NOT EXISTS "user"
(
    user_id            VARCHAR PRIMARY KEY,
    name               VARCHAR,
    yelping_since      TIMESTAMP,
    review_count       INT,
    average_stars      DOUBLE PRECISION,
    fans               INT,
    elite              VARCHAR,
    cool               INT,
    funny              INT,
    useful             INT,
    compliment_cool    INT,
    compliment_cute    INT,
    compliment_funny   INT,
    compliment_hot     INT,
    compliment_list    INT,
    compliment_more    INT,
    compliment_note    INT,
    compliment_photos  INT,
    compliment_plain   INT,
    compliment_profile INT,
    compliment_writer  INT
);

-- REVIEW TABLE
CREATE TABLE IF NOT EXISTS review
(
    review_id   VARCHAR PRIMARY KEY,
    business_id VARCHAR NOT NULL REFERENCES business (business_id) ON DELETE CASCADE,
    user_id     VARCHAR NOT NULL REFERENCES "user" (user_id) ON DELETE CASCADE,
    stars       INT,
    useful      INT,
    funny       INT,
    cool        INT,
    date        TIMESTAMP,
    text        TEXT
);

-- TIP TABLE
CREATE TABLE IF NOT EXISTS tip
(
    tip_id           SERIAL PRIMARY KEY,
    business_id      VARCHAR NOT NULL REFERENCES business (business_id) ON DELETE CASCADE,
    user_id          VARCHAR NOT NULL REFERENCES "user" (user_id) ON DELETE CASCADE,
    compliment_count INT,
    date             TIMESTAMP,
    text             TEXT
);

-- USER FRIENDS (SELF-JOIN TABLE)
CREATE TABLE IF NOT EXISTS user_friends
(
    user_id   VARCHAR NOT NULL REFERENCES "user" (user_id) ON DELETE CASCADE,
    friend_id VARCHAR NOT NULL REFERENCES "user" (user_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);