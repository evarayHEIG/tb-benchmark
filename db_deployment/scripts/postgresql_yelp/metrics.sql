-- Helper function to calculate statistics from an array of execution times
CREATE OR REPLACE FUNCTION public.calculate_statistics(times DOUBLE PRECISION[])
    RETURNS TABLE(
                     avg_time_ms DOUBLE PRECISION,
                     stddev_time_ms DOUBLE PRECISION,
                     variance_time_ms DOUBLE PRECISION,
                     percentile_95_ms DOUBLE PRECISION
                 ) AS $$
BEGIN
    -- Handle NULL or empty array edge case
    IF times IS NULL OR array_length(times, 1) = 0 THEN
        RETURN QUERY SELECT 0.0, 0.0, 0.0, 0.0;
        RETURN;
    END IF;

    IF array_length(times, 1) = 1 THEN
        -- If there's only one execution time, return it as the average and all other stats as 0
        RETURN QUERY SELECT times[1], 0.0::double precision, 0.0::double precision, times[1];
        RETURN;
    END IF;

    -- Use a single query with multiple aggregate functions for efficiency
    RETURN QUERY
        SELECT
            -- Use built-in aggregate functions
            avg(t.val),
            stddev_samp(t.val),  -- Calculates sample standard deviation (divides by n-1)
            var_samp(t.val),     -- Calculates sample variance (divides by n-1)

            -- Built-in percentile function (continuous/interpolated is usually preferred)
            percentile_cont(0.95) WITHIN GROUP (ORDER BY t.val)
        FROM
            unnest(times) AS t(val);

END
$$ LANGUAGE plpgsql;

-- Enhanced benchmark function that returns additional statistics
DROP FUNCTION IF EXISTS benchmark_query3(integer,text);
CREATE OR REPLACE FUNCTION public.benchmark_query3(n_runs INT, query TEXT)
    RETURNS TABLE(
                     avg_time_ms DOUBLE PRECISION,
                     stddev_time_ms DOUBLE PRECISION,
                     variance_time_ms DOUBLE PRECISION,
                     percentile_95_ms DOUBLE PRECISION,
                     explain_json JSON,
                     total_shared_hit_blocks BIGINT,
                     total_shared_read_blocks BIGINT,
                     avg_shared_hit_blocks DOUBLE PRECISION,
                     avg_shared_read_blocks DOUBLE PRECISION
                 ) AS $$
DECLARE
    execution_times DOUBLE PRECISION[] := '{}';
    i INT;
    explain_result JSON;
    first_explain_result JSON;
    execution_time DOUBLE PRECISION;
    stats RECORD;
    root_node JSONB;

    -- Buffer statistics per run
    s_hit BIGINT := 0;
    s_read BIGINT := 0;

    -- Accumulated buffer statistics
    total_s_hit BIGINT := 0;
    total_s_read BIGINT := 0;
BEGIN
    -- Run query n_runs times and collect execution times and buffer statistics
    FOR i IN 1..n_runs LOOP
            -- Execute query with EXPLAIN ANALYZE for each run
            EXECUTE format('EXPLAIN (ANALYZE, FORMAT JSON, BUFFERS) %s', query) INTO explain_result;

            -- Save the first explain result for output
            IF i = 1 THEN
                first_explain_result := explain_result;
            END IF;

            -- Extract execution time from JSON
            SELECT (explain_result->0->>'Execution Time')::DOUBLE PRECISION INTO execution_time;

            -- Get the root node of the plan
            SELECT (explain_result::jsonb->0->'Plan') INTO root_node;

            -- Extract buffer statistics from the root node
            s_hit := COALESCE((root_node->>'Shared Hit Blocks')::BIGINT, 0);
            s_read := COALESCE((root_node->>'Shared Read Blocks')::BIGINT, 0);

            -- Accumulate buffer statistics
            total_s_hit := total_s_hit + s_hit;
            total_s_read := total_s_read + s_read;

            -- Add to execution times array
            execution_times := array_append(execution_times, execution_time);
        END LOOP;

    -- Calculate statistics using the helper function
    FOR stats IN SELECT * FROM calculate_statistics(execution_times) LOOP
            -- Return the results with buffer statistics
            RETURN QUERY SELECT
                             stats.avg_time_ms,
                             stats.stddev_time_ms,
                             stats.variance_time_ms,
                             stats.percentile_95_ms,
                             first_explain_result,
                             total_s_hit,
                             total_s_read,
                             (total_s_hit::DOUBLE PRECISION / n_runs),
                             (total_s_read::DOUBLE PRECISION / n_runs);
        END LOOP;
END
$$ LANGUAGE plpgsql;

-- Helper function to calculate statistics from an array of execution times
CREATE OR REPLACE FUNCTION yelp_small.calculate_statistics(times DOUBLE PRECISION[])
    RETURNS TABLE(
                     avg_time_ms DOUBLE PRECISION,
                     stddev_time_ms DOUBLE PRECISION,
                     variance_time_ms DOUBLE PRECISION,
                     percentile_95_ms DOUBLE PRECISION
                 ) AS $$
BEGIN
    -- Handle NULL or empty array edge case
    IF times IS NULL OR array_length(times, 1) = 0 THEN
        RETURN QUERY SELECT 0.0, 0.0, 0.0, 0.0;
        RETURN;
    END IF;

    IF array_length(times, 1) = 1 THEN
        -- If there's only one execution time, return it as the average and all other stats as 0
        RETURN QUERY SELECT times[1], 0.0::double precision, 0.0::double precision, times[1];
        RETURN;
    END IF;

    -- Use a single query with multiple aggregate functions for efficiency
    RETURN QUERY
        SELECT
            -- Use built-in aggregate functions
            avg(t.val),
            stddev_samp(t.val),  -- Calculates sample standard deviation (divides by n-1)
            var_samp(t.val),     -- Calculates sample variance (divides by n-1)

            -- Built-in percentile function (continuous/interpolated is usually preferred)
            percentile_cont(0.95) WITHIN GROUP (ORDER BY t.val)
        FROM
            unnest(times) AS t(val);

END
$$ LANGUAGE plpgsql;

-- Enhanced benchmark function that returns additional statistics
DROP FUNCTION IF EXISTS yelp_small.benchmark_query3(integer,text);
CREATE OR REPLACE FUNCTION yelp_small.benchmark_query3(n_runs INT, query TEXT)
    RETURNS TABLE(
                     avg_time_ms DOUBLE PRECISION,
                     stddev_time_ms DOUBLE PRECISION,
                     variance_time_ms DOUBLE PRECISION,
                     percentile_95_ms DOUBLE PRECISION,
                     explain_json JSON,
                     total_shared_hit_blocks BIGINT,
                     total_shared_read_blocks BIGINT,
                     avg_shared_hit_blocks DOUBLE PRECISION,
                     avg_shared_read_blocks DOUBLE PRECISION
                 ) AS $$
DECLARE
    execution_times DOUBLE PRECISION[] := '{}';
    i INT;
    explain_result JSON;
    first_explain_result JSON;
    execution_time DOUBLE PRECISION;
    stats RECORD;
    root_node JSONB;

    -- Buffer statistics per run
    s_hit BIGINT := 0;
    s_read BIGINT := 0;

    -- Accumulated buffer statistics
    total_s_hit BIGINT := 0;
    total_s_read BIGINT := 0;
BEGIN
    -- Run query n_runs times and collect execution times and buffer statistics
    FOR i IN 1..n_runs LOOP
            -- Execute query with EXPLAIN ANALYZE for each run
            EXECUTE format('EXPLAIN (ANALYZE, FORMAT JSON, BUFFERS) %s', query) INTO explain_result;

            -- Save the first explain result for output
            IF i = 1 THEN
                first_explain_result := explain_result;
            END IF;

            -- Extract execution time from JSON
            SELECT (explain_result->0->>'Execution Time')::DOUBLE PRECISION INTO execution_time;

            -- Get the root node of the plan
            SELECT (explain_result::jsonb->0->'Plan') INTO root_node;

            -- Extract buffer statistics from the root node
            s_hit := COALESCE((root_node->>'Shared Hit Blocks')::BIGINT, 0);
            s_read := COALESCE((root_node->>'Shared Read Blocks')::BIGINT, 0);

            -- Accumulate buffer statistics
            total_s_hit := total_s_hit + s_hit;
            total_s_read := total_s_read + s_read;

            -- Add to execution times array
            execution_times := array_append(execution_times, execution_time);
        END LOOP;

    -- Calculate statistics using the helper function
    FOR stats IN SELECT * FROM calculate_statistics(execution_times) LOOP
            -- Return the results with buffer statistics
            RETURN QUERY SELECT
                             stats.avg_time_ms,
                             stats.stddev_time_ms,
                             stats.variance_time_ms,
                             stats.percentile_95_ms,
                             first_explain_result,
                             total_s_hit,
                             total_s_read,
                             (total_s_hit::DOUBLE PRECISION / n_runs),
                             (total_s_read::DOUBLE PRECISION / n_runs);
        END LOOP;
END
$$ LANGUAGE plpgsql;

-- Helper function to calculate statistics from an array of execution times
CREATE OR REPLACE FUNCTION yelp_medium.calculate_statistics(times DOUBLE PRECISION[])
    RETURNS TABLE(
                     avg_time_ms DOUBLE PRECISION,
                     stddev_time_ms DOUBLE PRECISION,
                     variance_time_ms DOUBLE PRECISION,
                     percentile_95_ms DOUBLE PRECISION
                 ) AS $$
BEGIN
    -- Handle NULL or empty array edge case
    IF times IS NULL OR array_length(times, 1) = 0 THEN
        RETURN QUERY SELECT 0.0, 0.0, 0.0, 0.0;
        RETURN;
    END IF;

    IF array_length(times, 1) = 1 THEN
        -- If there's only one execution time, return it as the average and all other stats as 0
        RETURN QUERY SELECT times[1], 0.0::double precision, 0.0::double precision, times[1];
        RETURN;
    END IF;

    -- Use a single query with multiple aggregate functions for efficiency
    RETURN QUERY
        SELECT
            -- Use built-in aggregate functions
            avg(t.val),
            stddev_samp(t.val),  -- Calculates sample standard deviation (divides by n-1)
            var_samp(t.val),     -- Calculates sample variance (divides by n-1)

            -- Built-in percentile function (continuous/interpolated is usually preferred)
            percentile_cont(0.95) WITHIN GROUP (ORDER BY t.val)
        FROM
            unnest(times) AS t(val);

END
$$ LANGUAGE plpgsql;

-- Enhanced benchmark function that returns additional statistics
DROP FUNCTION IF EXISTS yelp_medium.benchmark_query3(integer,text);
CREATE OR REPLACE FUNCTION yelp_medium.benchmark_query3(n_runs INT, query TEXT)
    RETURNS TABLE(
                     avg_time_ms DOUBLE PRECISION,
                     stddev_time_ms DOUBLE PRECISION,
                     variance_time_ms DOUBLE PRECISION,
                     percentile_95_ms DOUBLE PRECISION,
                     explain_json JSON,
                     total_shared_hit_blocks BIGINT,
                     total_shared_read_blocks BIGINT,
                     avg_shared_hit_blocks DOUBLE PRECISION,
                     avg_shared_read_blocks DOUBLE PRECISION
                 ) AS $$
DECLARE
    execution_times DOUBLE PRECISION[] := '{}';
    i INT;
    explain_result JSON;
    first_explain_result JSON;
    execution_time DOUBLE PRECISION;
    stats RECORD;
    root_node JSONB;

    -- Buffer statistics per run
    s_hit BIGINT := 0;
    s_read BIGINT := 0;

    -- Accumulated buffer statistics
    total_s_hit BIGINT := 0;
    total_s_read BIGINT := 0;
BEGIN
    -- Run query n_runs times and collect execution times and buffer statistics
    FOR i IN 1..n_runs LOOP
            -- Execute query with EXPLAIN ANALYZE for each run
            EXECUTE format('EXPLAIN (ANALYZE, FORMAT JSON, BUFFERS) %s', query) INTO explain_result;

            -- Save the first explain result for output
            IF i = 1 THEN
                first_explain_result := explain_result;
            END IF;

            -- Extract execution time from JSON
            SELECT (explain_result->0->>'Execution Time')::DOUBLE PRECISION INTO execution_time;

            -- Get the root node of the plan
            SELECT (explain_result::jsonb->0->'Plan') INTO root_node;

            -- Extract buffer statistics from the root node
            s_hit := COALESCE((root_node->>'Shared Hit Blocks')::BIGINT, 0);
            s_read := COALESCE((root_node->>'Shared Read Blocks')::BIGINT, 0);

            -- Accumulate buffer statistics
            total_s_hit := total_s_hit + s_hit;
            total_s_read := total_s_read + s_read;

            -- Add to execution times array
            execution_times := array_append(execution_times, execution_time);
        END LOOP;

    -- Calculate statistics using the helper function
    FOR stats IN SELECT * FROM calculate_statistics(execution_times) LOOP
            -- Return the results with buffer statistics
            RETURN QUERY SELECT
                             stats.avg_time_ms,
                             stats.stddev_time_ms,
                             stats.variance_time_ms,
                             stats.percentile_95_ms,
                             first_explain_result,
                             total_s_hit,
                             total_s_read,
                             (total_s_hit::DOUBLE PRECISION / n_runs),
                             (total_s_read::DOUBLE PRECISION / n_runs);
        END LOOP;
END
$$ LANGUAGE plpgsql;