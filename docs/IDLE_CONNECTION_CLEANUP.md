# Idle Connection 자동 정리 가이드

## 문제 상황
10분 이상 지난 Idle Connection이 20개 이상 누적되어 해소되지 않는 경우

## 원인 분석

### 1. HikariCP idle-timeout이 작동하지 않는 경우
- **원인**: HikariCP의 `idle-timeout`은 `minimum-idle`보다 작으면 작동하지 않음
- **현재 설정**: `minimum-idle=2`, `idle-timeout=300000` (5분)
- **문제**: Transaction Pooler 모드에서는 Connection이 애플리케이션에 반환되지 않고 Supabase에 남아있을 수 있음

### 2. Transaction Pooler의 Connection 관리 방식
- Transaction Pooler는 트랜잭션 종료 시 Connection을 즉시 반환해야 하지만
- 실제로는 Connection이 Supabase 레벨에서 idle 상태로 유지될 수 있음
- 애플리케이션 레벨(HikariCP)에서는 Connection이 반환된 것으로 보이지만
- 데이터베이스 레벨에서는 여전히 Connection이 유지됨

### 3. 트랜잭션이 완료되지 않은 경우
- 트랜잭션 타임아웃이 발생해도 Connection이 즉시 반환되지 않을 수 있음
- 예외 발생 시 Connection이 제대로 반환되지 않을 수 있음

## 해결 방법

### 방법 1: HikariCP 설정 조정 (이미 적용됨)

```properties
# idle-timeout을 minimum-idle보다 크게 설정
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.idle-timeout=300000  # 5분
spring.datasource.hikari.max-lifetime=300000  # 5분
```

**주의**: `idle-timeout`은 `minimum-idle`보다 작으면 작동하지 않습니다!

### 방법 2: PostgreSQL에서 수동 정리 (즉시 해결)

Supabase SQL Editor에서 실행:

```sql
-- 1. 10분 이상 지난 Idle Connection 확인
SELECT
    pid,
    application_name,
    state,
    NOW() - query_start as idle_duration
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '10 minutes';

-- 2. 강제 종료
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '10 minutes'
  AND pid != pg_backend_pid();
```

### 방법 3: 자동 모니터링 및 경고 (이미 적용됨)

`HikariConnectionPoolMonitor`가 2분마다 다음을 수행:
- Idle Connection이 5개 이상이고 Pool이 80% 이상 사용 중이면 경고
- PostgreSQL에서 수동 정리 필요 시 SQL 쿼리 제공

### 방법 4: 애플리케이션 재시작 (임시 해결)

가장 간단하지만 근본적인 해결책은 아닙니다.

## 모니터링

### 1. 애플리케이션 로그 확인

```bash
# Idle Connection 경고 확인
grep "Idle Connection이.*개 유지" application.log

# Connection 생성/감소 로그 확인
grep "Connection이.*개 증가\|감소" application.log
```

### 2. PostgreSQL에서 실시간 모니터링

```sql
-- 현재 Idle Connection 상태
SELECT
    COUNT(*) as idle_count,
    MAX(NOW() - query_start) as max_idle_duration,
    AVG(EXTRACT(EPOCH FROM (NOW() - query_start))) as avg_idle_seconds
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state = 'idle'
  AND application_name = 'PostgreSQL JDBC Driver';
```

### 3. Connection 생성 시점 분석

```sql
-- Connection이 언제 생성되었는지 확인
SELECT
    DATE_TRUNC('minute', backend_start) as creation_minute,
    COUNT(*) as connections_created
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND state = 'idle'
GROUP BY DATE_TRUNC('minute', backend_start)
ORDER BY creation_minute;
```

## 예방 방법

### 1. 트랜잭션 범위 최소화
- 필요한 경우에만 `@Transactional` 사용
- 읽기 전용 작업은 `@Transactional(readOnly = true)` 사용

### 2. 외부 API 호출 분리
- 트랜잭션 안에서 외부 API 호출 금지
- 외부 API 호출 후 트랜잭션 시작

### 3. 적절한 타임아웃 설정
- 전역 트랜잭션 타임아웃: 30초
- 긴 작업은 개별 타임아웃 설정

### 4. 정기적인 Connection 정리
- 주기적으로 PostgreSQL에서 오래된 Idle Connection 정리
- 또는 애플리케이션 재시작

## 체크리스트

- [ ] HikariCP 설정 확인 (`idle-timeout` > `minimum-idle`)
- [ ] PostgreSQL에서 10분 이상 지난 Idle Connection 확인
- [ ] 필요시 수동으로 Connection 종료
- [ ] 애플리케이션 로그에서 Connection leak 경고 확인
- [ ] 트랜잭션 타임아웃 발생 여부 확인
- [ ] 외부 API 호출이 트랜잭션 안에 있는지 확인

## 참고

- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Supabase Connection Pooling](https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooler)
- `/docs/sql-connection-management.sql` - Connection 관리 SQL 쿼리 모음

