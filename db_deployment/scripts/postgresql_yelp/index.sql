-- Index sur les foreign keys non déjà indexées par un PRIMARY KEY

-- Table business_categories : business_id
CREATE INDEX idx_business_categories_business_id ON public.business_categories(business_id);
CREATE INDEX idx_business_categories_business_id ON yelp_medium.business_categories(business_id);
CREATE INDEX idx_business_categories_business_id ON yelp_small.business_categories(business_id);

-- Table business_hours : business_id
CREATE INDEX idx_business_hours_business_id ON public.business_hours(business_id);
CREATE INDEX idx_business_hours_business_id ON yelp_medium.business_hours(business_id);
CREATE INDEX idx_business_hours_business_id ON yelp_small.business_hours(business_id);

-- Table checkin : business_id
CREATE INDEX idx_checkin_business_id ON public.checkin(business_id);
CREATE INDEX idx_checkin_business_id ON yelp_medium.checkin(business_id);
CREATE INDEX idx_checkin_business_id ON yelp_small.checkin(business_id);

-- Table checkin_date : checkin_id
CREATE INDEX idx_checkin_date_checkin_id ON public.checkin_date(checkin_id);
CREATE INDEX idx_checkin_date_checkin_id ON yelp_medium.checkin_date(checkin_id);
CREATE INDEX idx_checkin_date_checkin_id ON yelp_small.checkin_date(checkin_id);

-- Table review : business_id
CREATE INDEX idx_review_business_id ON public.review(business_id);
CREATE INDEX idx_review_business_id ON yelp_medium.review(business_id);
CREATE INDEX idx_review_business_id ON yelp_small.review(business_id);

-- Table review : user_id
CREATE INDEX idx_review_user_id ON public.review(user_id);
CREATE INDEX idx_review_user_id ON yelp_medium.review(user_id);
CREATE INDEX idx_review_user_id ON yelp_small.review(user_id);

-- Table tip : business_id
CREATE INDEX idx_tip_business_id ON public.tip(business_id);
CREATE INDEX idx_tip_business_id ON yelp_medium.tip(business_id);
CREATE INDEX idx_tip_business_id ON yelp_small.tip(business_id);

-- Table tip : user_id
CREATE INDEX idx_tip_user_id ON public.tip(user_id);
CREATE INDEX idx_tip_user_id ON yelp_medium.tip(user_id);
CREATE INDEX idx_tip_user_id ON yelp_small.tip(user_id);

-- Table user_friends : friend_id
CREATE INDEX idx_user_friends_friend_id ON public.user_friends(friend_id);
CREATE INDEX idx_user_friends_friend_id ON yelp_medium.user_friends(friend_id);
CREATE INDEX idx_user_friends_friend_id ON yelp_small.user_friends(friend_id);