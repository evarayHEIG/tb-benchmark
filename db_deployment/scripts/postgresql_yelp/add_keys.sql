-- -----------------------------
-- Add primary keys
-- -----------------------------

-- yelp_small schema
-- Primary keys
ALTER TABLE yelp_small.business ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.user ADD PRIMARY KEY (user_id);
ALTER TABLE yelp_small.review ADD PRIMARY KEY (review_id);
ALTER TABLE yelp_small.tip ADD PRIMARY KEY (tip_id);
ALTER TABLE yelp_small.checkin ADD PRIMARY KEY (checkin_id);
ALTER TABLE yelp_small.checkin_date ADD PRIMARY KEY (checkin_id, date);
ALTER TABLE yelp_small.user_friends ADD PRIMARY KEY (user_id, friend_id);
ALTER TABLE yelp_small.business_categories ADD PRIMARY KEY (business_id, category);
ALTER TABLE yelp_small.business_hours ADD PRIMARY KEY (business_id, day);
ALTER TABLE yelp_small.business_attributes ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.ambience ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.best_nights ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.business_parking ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.dietary_restrictions ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.good_for_meal ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.hair_specializes_in ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_small.music ADD PRIMARY KEY (business_id);

-- yelp_medium schema
-- PRIMARY KEYS

ALTER TABLE yelp_medium.business ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.review ADD PRIMARY KEY (review_id);
ALTER TABLE yelp_medium.checkin ADD PRIMARY KEY (checkin_id);
ALTER TABLE yelp_medium.tip ADD PRIMARY KEY (tip_id);
ALTER TABLE yelp_medium."user" ADD PRIMARY KEY (user_id);
ALTER TABLE yelp_medium.ambience ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.best_nights ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.business_attributes ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.business_categories ADD PRIMARY KEY (business_id, category);
ALTER TABLE yelp_medium.business_hours ADD PRIMARY KEY (business_id, day);
ALTER TABLE yelp_medium.business_parking ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.dietary_restrictions ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.good_for_meal ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.hair_specializes_in ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.music ADD PRIMARY KEY (business_id);
ALTER TABLE yelp_medium.user_friends ADD PRIMARY KEY (user_id, friend_id);
ALTER TABLE yelp_medium.checkin_date ADD PRIMARY KEY (checkin_id, date);