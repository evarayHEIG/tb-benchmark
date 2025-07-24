-- SQL Script to update PostgreSQL configuration parameters

-- Update shared_buffers
-- Represents about 30% of available RAM
ALTER SYSTEM SET shared_buffers = '8GB';

-- Update effective_cache_size
-- Estimation of memory available for disk caching
ALTER SYSTEM SET effective_cache_size = '18GB';

-- Update maintenance_work_mem
-- Memory used for maintenance operations (VACUUM, CREATE INDEX, etc.)
ALTER SYSTEM SET maintenance_work_mem = '1GB';

-- Update work_mem
-- Memory used for sort and hash operations
ALTER SYSTEM SET work_mem = '64MB';

-- Update maximum number of parallel workers
ALTER SYSTEM SET max_parallel_workers = 4;

-- Update maximum number of parallel workers per gather
ALTER SYSTEM SET max_parallel_workers_per_gather = 0;

-- Update maximum number of worker processes
ALTER SYSTEM SET max_worker_processes = 8;

-- Reload configuration without restarting
SELECT pg_reload_conf();