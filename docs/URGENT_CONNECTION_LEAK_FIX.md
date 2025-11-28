# 🚨 긴급: Connection Leak 해결 (Idle 40개)

## 현재 상황
- Idle Connection이 40개까지 증가
- 이전 해결책들이 작동하지 않음
- Connection이 계속 누적되고 있음

## 즉시 조치 사항

### 1. PostgreSQL에서 Connection 상태 확인

```sql
-- Connection 상태 상세 분석
SELECT 
  pid,
  application_name,
  state,
  CASE 
    WHEN state = 'idle' THEN '유휴 상태 (사용 가능)'
    WHEN state = 'idle in transaction' THEN '⚠️ 트랜잭션 중 유휴 (문제!)'
    WHEN state = 'active' THEN '사용 중'
    ELSE state
  END as state_description,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration,
  query
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
ORDER BY backend_start;
```

**중요**: `idle in transaction` 상태가 있다면 이것이 문제의 원인입니다!

### 2. 즉시 Connection 정리 (긴급)

```sql
-- 5분 이상 idle 상태인 Connection 모두 종료
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND state IN ('idle', 'idle in transaction')
  AND application_name = 'PostgreSQL JDBC Driver'
  AND NOW() - query_start > INTERVAL '5 minutes'
  AND pid != pg_backend_pid();
```

### 3. 애플리케이션 설정 변경

#### 변경 사항 1: maximum-pool-size 감소
```properties
# 20에서 10으로 감소
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=0
```

#### 변경 사항 2: 자동 Connection 정리 활성화
애플리케이션에서 2분마다 자동으로 오래된 Connection을 정리합니다.

## 근본 원인 재분석

### 가능한 원인 1: "idle in transaction" 상태
트랜잭션이 시작되었지만 완료되지 않은 상태로 Connection이 유지됨

**확인 방법:**
```sql
SELECT COUNT(*) 
FROM pg_stat_activity 
WHERE state = 'idle in transaction'
  AND application_name = 'PostgreSQL JDBC Driver';
```

**해결:**
- 트랜잭션 타임아웃 확인 (현재 30초)
- 예외 발생 시 롤백 확인
- 외부 API 호출이 트랜잭션 안에 있는지 확인

### 가능한 원인 2: Supabase Pooler의 Connection 관리 문제
Transaction Pooler가 Connection을 제대로 반환하지 않음

**해결:**
- Direct Connection 사용 고려
- 또는 Supabase 지원팀에 문의

### 가능한 원인 3: connection-init-sql이 적용되지 않음
`idle_in_transaction_session_timeout` 설정이 실제로 적용되지 않았을 수 있음

**확인 방법:**
```sql
-- 현재 설정 확인
SHOW idle_in_transaction_session_timeout;
```

**해결:**
- Supabase에서 데이터베이스 레벨 설정 확인
- 또는 Supabase 지원팀에 문의

## 추가 해결책

### Option 1: Direct Connection 사용 (Pooler 우회)

```properties
# Direct Connection 사용 (Pooler 제거)
# 주의: Supabase의 Connection 제한에 직접 영향
spring.datasource.hikari.jdbc-url=jdbc:postgresql://db.jxbmvvqjilxblzrojkek.supabase.co:5432/postgres
```

**장점:**
- HikariCP가 Connection을 완전히 제어
- Connection leak 추적이 쉬움

**단점:**
- Max Client Connections (200개) 제한에 직접 영향
- Connection 수가 많아질 수 있음

### Option 2: Supabase 지원팀에 문의

다음 정보를 포함하여 문의:
1. Connection이 계속 누적되는 문제
2. `idle_in_transaction_session_timeout` 설정 가능 여부
3. Transaction Pooler의 Connection 반환 문제
4. 데이터베이스 레벨 설정 권한

### Option 3: 애플리케이션 재시작

임시 해결책이지만 즉시 효과가 있습니다.

## 모니터링

### 실시간 Connection 상태 확인

```sql
-- 1분마다 실행하여 Connection 변화 추적
SELECT 
  state,
  COUNT(*) as count,
  MAX(NOW() - query_start) as max_idle_duration
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
GROUP BY state;
```

### 애플리케이션 로그 확인

```
🧹 오래된 Idle Connection 강제 정리 시작
✅ X개의 오래된 Idle Connection을 종료했습니다.
```

## 체크리스트

- [ ] PostgreSQL에서 Connection 상태 확인 (`idle` vs `idle in transaction`)
- [ ] 5분 이상 idle 상태인 Connection 즉시 종료
- [ ] 애플리케이션 설정 변경 (maximum-pool-size=10)
- [ ] 애플리케이션 재시작
- [ ] 2분 후 자동 정리 로그 확인
- [ ] Connection 수 모니터링 (감소하는지 확인)
- [ ] 필요시 Direct Connection 사용 검토
- [ ] Supabase 지원팀에 문의 (필요시)

## 예상 결과

1. **즉시**: PostgreSQL에서 수동으로 Connection 종료 → Connection 수 감소
2. **2분 후**: 애플리케이션 자동 정리 로직 실행 → 추가 Connection 종료
3. **지속적**: maximum-pool-size=10으로 제한 → 새로운 Connection 생성 제한
4. **장기적**: Connection leak 원인 분석 및 해결

## 다음 단계

1. 위의 SQL 쿼리로 Connection 상태 확인
2. 즉시 Connection 정리 실행
3. 애플리케이션 재시작
4. 2분 후 로그 확인
5. Connection 수가 감소하는지 모니터링
6. 여전히 증가하면 Direct Connection 사용 검토

