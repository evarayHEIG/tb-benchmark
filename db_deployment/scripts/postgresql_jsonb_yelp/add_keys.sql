-- -----------------------------
-- Add primary keys
-- -----------------------------

-- yelp_small schema
-- Primary keys
ALTER TABLE yelp_small.business
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_small."user"
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_small.review
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_small.checkin
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_small.tip
    ADD PRIMARY KEY (id);

-- yelp_medium schema
-- PRIMARY KEYS
ALTER TABLE yelp_medium.business
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_medium."user"
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_medium.review
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_medium.checkin
    ADD PRIMARY KEY (id);

ALTER TABLE yelp_medium.tip
    ADD PRIMARY KEY (id);
