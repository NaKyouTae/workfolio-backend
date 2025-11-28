-- ============================================
-- Supabase Connection 관리 SQL 쿼리 모음
-- ============================================

-- 1. 현재 모든 Connection 확인
SELECT 
  pid as connection_id,
  usename as connected_role,
  application_name,
  client_addr as ip,
  state,
  backend_start,
  query_start,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration,
  query
FROM pg_stat_activity
WHERE datname = 'postgres'
ORDER BY backend_start;

-- 2. Idle 상태인 Connection만 확인 (애플리케이션 연결)
-- ⚠️ 중요: 'idle' 상태는 "사용 중"이 아니라 "유휴 상태 = 사용 가능한 상태"입니다!
-- 'active' 상태가 실제로 "사용 중"인 상태입니다.
SELECT 
  pid as connection_id,
  usename as connected_role,
  application_name,
  client_addr as ip,
  state,
  backend_start,
  query_start,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration,
  query
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
ORDER BY backend_start;

-- 2-1. 전체 Connection 상태 확인 (active + idle)
-- 이 쿼리로 실제 사용 중인 Connection과 유휴 Connection을 모두 확인할 수 있습니다.
SELECT 
  state,
  COUNT(*) as connection_count,
  MIN(NOW() - backend_start) as min_duration,
  MAX(NOW() - backend_start) as max_duration
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
GROUP BY state
ORDER BY state;

-- 3. 오래 유지된 Idle Connection 확인 (10분 이상)
SELECT 
  pid as connection_id,
  usename as connected_role,
  application_name,
  client_addr as ip,
  state,
  backend_start,
  query_start,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration,
  query
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '10 minutes'
ORDER BY query_start;

-- 4. 특정 Connection 강제 종료 (pg_terminate_backend)
-- 주의: 이 함수는 Connection을 즉시 종료합니다. 진행 중인 트랜잭션은 롤백됩니다.
SELECT pg_terminate_backend(2997);  -- pid로 종료

-- 5. 여러 Connection 한 번에 종료 (10분 이상 idle인 Connection)
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '10 minutes'
  AND pid != pg_backend_pid();  -- 자신의 Connection은 제외

-- 6. 특정 IP의 모든 Connection 종료
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND client_addr = '2406:da12:b78:de00:6557:6524:94b4:9875'  -- IP 주소
  AND pid != pg_backend_pid();

-- 7. Connection 취소 (pg_cancel_backend) - 더 부드러운 종료
-- 주의: pg_terminate_backend보다 부드럽지만, idle Connection에는 효과가 없을 수 있습니다.
SELECT pg_cancel_backend(2997);  -- pid로 취소

-- 8. 현재 자신의 Connection ID 확인
SELECT pg_backend_pid();

-- 9. Connection 통계 확인
SELECT 
  state,
  COUNT(*) as connection_count,
  MIN(NOW() - backend_start) as min_duration,
  MAX(NOW() - backend_start) as max_duration,
  AVG(EXTRACT(EPOCH FROM (NOW() - backend_start))) as avg_duration_seconds
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
GROUP BY state;

-- 9-1. Connection 생성 시점 분석 (왜 정확히 10개가 생성되었는지 확인)
-- HikariCP는 minimum-idle만큼만 초기화하는 것이 아니라, 실제 사용량에 따라 동적으로 생성합니다.
-- 10개가 생성된 이유:
-- 1. 과거에 동시에 10개의 요청이 있었음
-- 2. Connection이 반환되지 않아서 누적됨 (Connection leak)
-- 3. 여러 번의 요청으로 인해 점진적으로 증가
SELECT 
  DATE_TRUNC('minute', backend_start) as creation_minute,
  COUNT(*) as connections_created,
  MIN(backend_start) as first_connection,
  MAX(backend_start) as last_connection
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND state = 'idle'
GROUP BY DATE_TRUNC('minute', backend_start)
ORDER BY creation_minute;

-- 10. Connection Leak 감지 (idle 상태가 5분 이상)
SELECT 
  pid,
  application_name,
  state,
  backend_start,
  query_start,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration,
  'Connection leak 가능성' as warning
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '5 minutes'
ORDER BY query_start;


-- 1단계: 종료할 Connection 확인
SELECT
    pid,
    application_name,
    NOW() - query_start as idle_duration
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '10 minutes';

-- 2단계: 확인 후 종료
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND pid != pg_backend_pid();

-- 3단계: 종료 확인
SELECT
    pid,
    application_name,
    state
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver';

-- ============================================
-- ⚠️ Connection Leak 해결: 자동 정리 스크립트
-- ============================================
-- HikariCP의 idle-timeout이 작동하지 않는 경우를 대비한 수동 정리
-- 주의: 이 쿼리는 애플리케이션의 Connection을 강제로 종료하므로 신중하게 사용

-- 1. 10분 이상 지난 Idle Connection 확인
SELECT
    pid,
    application_name,
    state,
    NOW() - query_start as idle_duration,
    query_start,
    backend_start
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '10 minutes'
ORDER BY query_start;

-- 2. 10분 이상 지난 Idle Connection 강제 종료
-- ⚠️ 주의: 현재 세션은 제외하고 종료
SELECT pg_terminate_backend(pid) as terminated_pid
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '10 minutes'
  AND pid != pg_backend_pid();

-- 3. 5분 이상 지난 Idle Connection 종료 (더 공격적인 정리)
-- HikariCP의 idle-timeout(5분)과 동일하게 설정
SELECT pg_terminate_backend(pid) as terminated_pid
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '5 minutes'
  AND pid != pg_backend_pid();

-- 4. 종료 후 상태 확인
SELECT
    COUNT(*) as remaining_idle_connections,
    MAX(NOW() - query_start) as max_idle_duration
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver';

-- ============================================
-- 자동 정리 함수 (선택사항)
-- ============================================
-- PostgreSQL 함수로 자동 정리 스케줄러 생성 (pg_cron 확장 필요)
-- 주의: Supabase에서는 pg_cron이 비활성화되어 있을 수 있음
/*
CREATE OR REPLACE FUNCTION cleanup_idle_connections()
RETURNS void AS $$
BEGIN
    PERFORM pg_terminate_backend(pid)
    FROM pg_stat_activity
    WHERE datname = 'postgres'
      AND state = 'idle'
      AND application_name = 'PostgreSQL JDBC Driver'
      AND NOW() - query_start > INTERVAL '10 minutes'
      AND pid != pg_backend_pid();
END;
$$ LANGUAGE plpgsql;

-- pg_cron으로 5분마다 실행 (Supabase에서는 사용 불가능할 수 있음)
-- SELECT cron.schedule('cleanup-idle-connections', '*/5 * * * *', 'SELECT cleanup_idle_connections()');
*/
  AND application_name = 'PostgreSQL JDBC Driver';