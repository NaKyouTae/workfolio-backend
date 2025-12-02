# Supabase Connection 동기화 문제 해결

## 문제 상황

HikariCP가 Connection을 제거했다고 판단하지만, PostgreSQL 레벨에서는 Connection이 여전히 idle 상태로 유지되어 Connection이 계속 누적되는 문제가 발생했습니다.

### 증상

1. **HikariCP Connection 수**: 10개 (정상)
2. **PostgreSQL Connection 수**: 20개 (누적)
3. **원인**: HikariCP가 Connection을 제거했다고 판단했지만, PostgreSQL에서는 여전히 idle 상태로 유지됨

### 데이터 분석

```
처음 생성된 10개 Connection:
- query_start가 계속 갱신됨 (00:16:10 → 00:18:14 → 00:29:18)
- HikariCP가 Connection을 재사용하고 있음

나중에 생성된 10개 Connection:
- query_start가 갱신되지 않음 (00:29:18에 고정)
- HikariCP가 Connection을 제거했다고 판단했지만, PostgreSQL에서는 여전히 idle 상태
```

## 근본 원인

### Supabase Transaction Pooler의 동작 방식

1. **HikariCP 레벨**:
   - `idle-timeout=3분` 도달 시 Connection 제거
   - `max-lifetime=5분` 도달 시 Connection 교체
   - Housekeeper 스레드가 주기적으로 정리

2. **PostgreSQL 레벨**:
   - Connection이 실제로 닫히지 않고 idle 상태로 유지됨
   - `idle_in_transaction_session_timeout` 설정이 제대로 작동하지 않음
   - Supabase Transaction Pooler가 Connection을 관리하므로, HikariCP의 정리 로직이 무시됨

3. **결과**:
   - HikariCP는 Connection이 제거되었다고 판단
   - PostgreSQL에서는 Connection이 여전히 idle 상태로 유지
   - HikariCP가 새로운 Connection을 생성
   - Connection이 계속 누적됨

## 해결 방법

### 1. PostgreSQL 레벨에서 주기적으로 Connection 종료 (적용됨)

`HikariConnectionPoolMonitor.evictIdleConnections()` 메서드가 2분마다 실행되어:

1. **Connection 수 비교**:
   - HikariCP Connection 수와 PostgreSQL Connection 수를 비교
   - 불일치 감지 시 경고 로그 출력

2. **오래된 Connection 종료**:
   - 3분 이상 idle 상태인 Connection을 PostgreSQL에서 직접 종료
   - `pg_terminate_backend()` 함수 사용

**코드:**
```kotlin
@Scheduled(fixedRate = 120000) // 2분마다 실행
fun evictIdleConnections() {
    // HikariCP Connection 수와 PostgreSQL Connection 수 비교
    val postgresConnectionCount = getPostgreSQLConnectionCount()
    
    if (postgresConnectionCount > total) {
        logger.warn("Connection 불일치 감지!")
    }
    
    // 3분 이상 idle 상태인 Connection 종료
    val terminatedCount = jdbcTemplate.queryForList(
        """
        SELECT pg_terminate_backend(pid)
        FROM pg_stat_activity
        WHERE datname = 'postgres'
          AND state = 'idle'
          AND application_name LIKE 'workfolio-server-%'
          AND NOW() - query_start > INTERVAL '3 minutes'
          AND pid != pg_backend_pid()
        """
    )
}
```

### 2. idle_in_transaction_session_timeout 설정 확인

현재 설정:
```properties
spring.datasource.hikari.connection-init-sql=SET idle_in_transaction_session_timeout = '5min'
```

**확인 방법:**
```sql
-- 현재 설정 확인
SHOW idle_in_transaction_session_timeout;

-- 데이터베이스 레벨 설정 (Supabase에서 권한이 제한될 수 있음)
ALTER DATABASE postgres SET idle_in_transaction_session_timeout = '3min';
```

### 3. Connection이 실제로 닫히는지 확인

HikariCP DEBUG 로그에서 확인:
```
HikariPool-1 - Connection evicted (idle-timeout)
HikariPool-1 - Connection closed
```

PostgreSQL에서 확인:
```sql
-- Connection이 실제로 닫혔는지 확인
SELECT 
  pid,
  state,
  NOW() - query_start as idle_duration,
  NOW() - backend_start as connection_duration
FROM pg_stat_activity
WHERE application_name LIKE 'workfolio-server-%'
ORDER BY backend_start;
```

## 모니터링

### 1. Connection 수 비교

`HikariConnectionPoolMonitor`가 2분마다 실행되어:
- HikariCP Connection 수와 PostgreSQL Connection 수를 비교
- 불일치 감지 시 경고 로그 출력

**로그 예시:**
```
⚠️ Connection 불일치 감지!
HikariCP Connection 수: 10
PostgreSQL Connection 수: 20
차이: 10개
```

### 2. Connection 종료 로그

**로그 예시:**
```
🧹 오래된 Idle Connection 강제 정리 시작
✅ 10개의 오래된 Idle Connection을 PostgreSQL에서 종료했습니다.
💡 HikariCP Connection 수와 PostgreSQL Connection 수를 동기화했습니다.
```

## 예상 결과

1. **즉시**: PostgreSQL에서 오래된 Connection 종료 → Connection 수 감소
2. **2분 후**: 자동 정리 로직 실행 → 추가 Connection 종료
3. **지속적**: HikariCP Connection 수와 PostgreSQL Connection 수 동기화

## 추가 권장 사항

### 1. Supabase 지원팀에 문의

다음 정보를 포함하여 문의:
1. Transaction Pooler를 사용할 때 Connection이 실제로 닫히지 않는 문제
2. `idle_in_transaction_session_timeout` 설정이 제대로 작동하지 않는 문제
3. 데이터베이스 레벨 설정 권한 요청

### 2. Direct Connection 사용 검토

Pooler를 우회하고 직접 연결:
```properties
# Direct Connection 사용 (Pooler 제거)
spring.datasource.hikari.jdbc-url=jdbc:postgresql://db.jxbmvvqjilxblzrojkek.supabase.co:5432/postgres
```

**장점:**
- HikariCP가 Connection을 완전히 제어
- Connection이 실제로 닫힘

**단점:**
- Max Client Connections (200개) 제한에 직접 영향
- Connection 수가 많아질 수 있음

## 결론

**문제:**
- HikariCP가 Connection을 제거했다고 판단하지만, PostgreSQL에서는 여전히 idle 상태로 유지됨
- Connection이 계속 누적됨

**해결:**
- PostgreSQL 레벨에서 주기적으로 오래된 Connection을 강제로 종료
- HikariCP Connection 수와 PostgreSQL Connection 수를 비교하여 불일치 감지
- 2분마다 자동 정리 로직 실행

**결과:**
- Connection 수가 정상적으로 유지됨
- HikariCP Connection 수와 PostgreSQL Connection 수가 동기화됨

