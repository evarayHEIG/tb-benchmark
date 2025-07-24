-- Add missing statistics for jsonb columns in Yelp dataset

-- Create statistics for public schema
-- Create statistics for business table
CREATE STATISTICS IF NOT EXISTS business_id_stats ON ((data ->>'business_id')::text) FROM public.business;
CREATE STATISTICS IF NOT EXISTS business_city_stats ON (data ->>'city'::text) FROM public.business;

-- Create statistics for checkin table
CREATE STATISTICS IF NOT EXISTS checkin_business_id_stats ON (data ->>'business_id'::text) FROM public.checkin;

-- Create statistics for review table
CREATE STATISTICS IF NOT EXISTS review_business_id_stats ON (data ->>'business_id'::text) FROM public.review;
CREATE STATISTICS IF NOT EXISTS review_id_stats ON (data ->>'review_id'::text) FROM public.review;
CREATE STATISTICS IF NOT EXISTS review_user_id_stats ON (data ->>'user_id'::text) FROM public.review;

-- Create statistics for tip table
CREATE STATISTICS IF NOT EXISTS tip_business_id_stats ON (data ->>'business_id'::text) FROM public.tip;
CREATE STATISTICS IF NOT EXISTS tip_user_id_stats ON (data ->>'user_id'::text) FROM public.tip;

-- Create statistics for user table
CREATE STATISTICS IF NOT EXISTS user_id_stats ON (data ->>'user_id'::text) FROM public."user";

-- Analyze tables to update statistics
ANALYZE public.business;
ANALYZE public.checkin;
ANALYZE public.review;
ANALYZE public.tip;
ANALYZE public."user";

-- Schéma yelp_small
CREATE STATISTICS IF NOT EXISTS small_business_id_stats ON (data ->>'business_id'::text) FROM yelp_small.business;
CREATE STATISTICS IF NOT EXISTS small_business_city_stats ON (data ->>'city'::text) FROM yelp_small.business;
CREATE STATISTICS IF NOT EXISTS small_checkin_business_id_stats ON (data ->>'business_id'::text) FROM yelp_small.checkin;
CREATE STATISTICS IF NOT EXISTS small_review_business_id_stats ON (data ->>'business_id'::text) FROM yelp_small.review;
CREATE STATISTICS IF NOT EXISTS small_review_id_stats ON (data ->>'review_id'::text) FROM yelp_small.review;
CREATE STATISTICS IF NOT EXISTS small_review_user_id_stats ON (data ->>'user_id'::text) FROM yelp_small.review;
CREATE STATISTICS IF NOT EXISTS small_tip_business_id_stats ON (data ->>'business_id'::text) FROM yelp_small.tip;
CREATE STATISTICS IF NOT EXISTS small_tip_user_id_stats ON (data ->>'user_id'::text) FROM yelp_small.tip;
CREATE STATISTICS IF NOT EXISTS small_user_id_stats ON (data ->>'user_id'::text) FROM yelp_small."user";

ANALYZE yelp_small.business;
ANALYZE yelp_small.checkin;
ANALYZE yelp_small.review;
ANALYZE yelp_small.tip;
ANALYZE yelp_small."user";

-- Schéma yelp_medium
CREATE STATISTICS IF NOT EXISTS medium_business_id_stats ON (data ->>'business_id'::text) FROM yelp_medium.business;
CREATE STATISTICS IF NOT EXISTS medium_business_city_stats ON (data ->>'city'::text) FROM yelp_medium.business;
CREATE STATISTICS IF NOT EXISTS medium_checkin_business_id_stats ON (data ->>'business_id'::text) FROM yelp_medium.checkin;
CREATE STATISTICS IF NOT EXISTS medium_review_business_id_stats ON (data ->>'business_id'::text) FROM yelp_medium.review;
CREATE STATISTICS IF NOT EXISTS medium_review_id_stats ON (data ->>'review_id'::text) FROM yelp_medium.review;
CREATE STATISTICS IF NOT EXISTS medium_review_user_id_stats ON (data ->>'user_id'::text) FROM yelp_medium.review;
CREATE STATISTICS IF NOT EXISTS medium_tip_business_id_stats ON (data ->>'business_id'::text) FROM yelp_medium.tip;
CREATE STATISTICS IF NOT EXISTS medium_tip_user_id_stats ON (data ->>'user_id'::text) FROM yelp_medium.tip;
CREATE STATISTICS IF NOT EXISTS medium_user_id_stats ON (data ->>'user_id'::text) FROM yelp_medium."user";

ANALYZE yelp_medium.business;
ANALYZE yelp_medium.checkin;
ANALYZE yelp_medium.review;
ANALYZE yelp_medium.tip;
ANALYZE yelp_medium."user";
