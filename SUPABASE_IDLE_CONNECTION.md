SELECT 
  pid,
  application_name,
  state,
  backend_start,
  query_start,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration
FROM pg_stat_activity
WHERE application_name = 'PostgreSQL JDBC Driver'
  AND state = 'idle'
ORDER BY backend_start;