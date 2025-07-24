-- Index sur business_id pour chaque schéma
CREATE INDEX idx_public_business_id ON public.business USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_medium_business_id ON yelp_medium.business USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_small_business_id ON yelp_small.business USING BTREE ((data ->> 'business_id'));

-- Index sur user_id pour chaque schéma
CREATE INDEX idx_public_user_id ON public."user" USING BTREE ((data ->> 'user_id'));
CREATE INDEX idx_medium_user_id ON yelp_medium."user" USING BTREE ((data ->> 'user_id'));
CREATE INDEX idx_small_user_id ON yelp_small."user" USING BTREE ((data ->> 'user_id'));

-- Index sur checkin.business_id pour chaque schéma
CREATE INDEX idx_public_checkin_business_id ON public.checkin USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_medium_checkin_business_id ON yelp_medium.checkin USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_small_checkin_business_id ON yelp_small.checkin USING BTREE ((data ->> 'business_id'));

-- Index sur review.business_id pour chaque schéma
CREATE INDEX idx_public_review_business_id ON public.review USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_medium_review_business_id ON yelp_medium.review USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_small_review_business_id ON yelp_small.review USING BTREE ((data ->> 'business_id'));

-- Index sur review.user_id pour chaque schéma
CREATE INDEX idx_public_review_user_id ON public.review USING BTREE ((data ->> 'user_id'));
CREATE INDEX idx_medium_review_user_id ON yelp_medium.review USING BTREE ((data ->> 'user_id'));
CREATE INDEX idx_small_review_user_id ON yelp_small.review USING BTREE ((data ->> 'user_id'));

-- Index sur tip.business_id pour chaque schéma
CREATE INDEX idx_public_tip_business_id ON public.tip USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_medium_tip_business_id ON yelp_medium.tip USING BTREE ((data ->> 'business_id'));
CREATE INDEX idx_small_tip_business_id ON yelp_small.tip USING BTREE ((data ->> 'business_id'));

-- Index sur tip.user_id pour chaque schéma
CREATE INDEX idx_public_tip_user_id ON public.tip USING BTREE ((data ->> 'user_id'));
CREATE INDEX idx_medium_tip_user_id ON yelp_medium.tip USING BTREE ((data ->> 'user_id'));
CREATE INDEX idx_small_tip_user_id ON yelp_small.tip USING BTREE ((data ->> 'user_id'));