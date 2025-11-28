# Supabase Connection Leak 근본 원인 및 해결책

## 🔍 근본 원인 분석

### 문제의 핵심
**Supabase Transaction Pooler (port 5432)를 사용하더라도, Connection이 PostgreSQL 레벨에서 idle 상태로 유지되고 있습니다.**

이는 다음 이유 때문입니다:

1. **Transaction Pooler의 동작 방식**
   - Transaction Pooler는 트랜잭션이 끝나면 Connection을 즉시 반환해야 하지만
   - 실제로는 Connection이 Supabase의 pgbouncer 레벨에서 idle 상태로 유지됨
   - HikariCP는 Connection이 반환된 것으로 인식하지만, PostgreSQL에서는 여전히 Connection이 살아있음

2. **HikariCP의 idle-timeout 한계**
   - HikariCP의 `idle-timeout`은 **애플리케이션 레벨**에서만 작동
   - PostgreSQL 레벨의 idle Connection은 HikariCP가 제어할 수 없음
   - Supabase Pooler가 Connection을 관리하므로, HikariCP의 정리 로직이 무시될 수 있음

3. **PostgreSQL의 idle_in_transaction_session_timeout 미설정**
   - PostgreSQL은 기본적으로 idle 트랜잭션을 자동으로 종료하지 않음
   - `idle_in_transaction_session_timeout` 설정이 없으면 Connection이 무한정 유지됨

## ✅ 근본적인 해결책

### 해결책 1: PostgreSQL idle_in_transaction_session_timeout 설정 (가장 중요!)

**Supabase SQL Editor에서 실행:**

```sql
-- 현재 설정 확인
SHOW idle_in_transaction_session_timeout;

-- 5분 이상 idle 상태인 트랜잭션 자동 종료 설정
ALTER DATABASE postgres SET idle_in_transaction_session_timeout = '5min';

-- 또는 세션 레벨에서 설정 (즉시 적용)
SET idle_in_transaction_session_timeout = '5min';
```

**주의사항:**
- Supabase는 관리형 서비스이므로 데이터베이스 레벨 설정이 제한될 수 있음
- 세션 레벨 설정은 현재 세션에만 적용되므로, 애플리케이션 시작 시 실행해야 함
- Supabase 대시보드에서 데이터베이스 설정을 확인하거나, Supabase 지원팀에 문의 필요

### 해결책 2: Direct Connection 사용 (Pooler 우회)

**장점:**
- Connection이 애플리케이션에서 직접 관리됨
- HikariCP의 idle-timeout이 정상 작동
- Connection leak 추적이 쉬움

**단점:**
- Supabase의 Connection Pool Size 제한에 직접 영향
- Max Client Connections (200개) 제한에 직접 영향
- Connection 수가 많아질 수 있음

**설정 방법:**
```properties
# Direct Connection 사용 (Pooler 우회)
# 포트 5432 대신 Direct Connection 포트 사용 (Supabase 문서 확인 필요)
# 또는 Connection String에서 pooler 제거
spring.datasource.hikari.jdbc-url=jdbc:postgresql://db.jxbmvvqjilxblzrojkek.supabase.co:5432/postgres
```

### 해결책 3: Connection Validation 강화

HikariCP가 Connection을 사용하기 전에 유효성을 검사하도록 설정:

```properties
# Connection 사용 전 항상 검증
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=5000

# Connection이 유효하지 않으면 즉시 제거
spring.datasource.hikari.keepalive-time=30000  # 30초마다 keepalive
```

### 해결책 4: 애플리케이션 시작 시 PostgreSQL 설정 적용

애플리케이션 시작 시 자동으로 `idle_in_transaction_session_timeout` 설정:

```kotlin
@PostConstruct
fun configurePostgreSQLTimeout() {
    // 애플리케이션 시작 시 PostgreSQL 설정 적용
    // 주의: Supabase에서는 제한될 수 있음
}
```

### 해결책 5: 주기적인 Connection 정리 (자동화)

애플리케이션에서 주기적으로 오래된 Connection을 정리하는 스케줄러:

```kotlin
@Scheduled(fixedRate = 300000) // 5분마다
fun cleanupIdleConnections() {
    // PostgreSQL에서 직접 Connection 종료
    // 주의: Supabase에서는 권한이 제한될 수 있음
}
```

## 🎯 권장 해결 순서

### 1단계: PostgreSQL 설정 확인 및 적용 (최우선)

```sql
-- Supabase SQL Editor에서 실행
SET idle_in_transaction_session_timeout = '5min';

-- 영구적으로 적용하려면 (Supabase 권한 필요)
ALTER DATABASE postgres SET idle_in_transaction_session_timeout = '5min';
```

### 2단계: HikariCP 설정 최적화

```properties
# Connection 정리를 더 공격적으로
spring.datasource.hikari.minimum-idle=1  # 최소값으로 설정
spring.datasource.hikari.idle-timeout=180000  # 3분
spring.datasource.hikari.max-lifetime=300000  # 5분
spring.datasource.hikari.keepalive-time=30000  # 30초마다 keepalive
```

### 3단계: 애플리케이션 시작 시 설정 적용

애플리케이션 시작 시 자동으로 PostgreSQL 설정 적용 (해결책 4)

### 4단계: 모니터링 및 자동 정리

주기적으로 Connection 상태를 모니터링하고 필요시 정리 (해결책 5)

## 📊 Supabase Connection Pooler 동작 이해

### Transaction Pooler (port 5432)
- **목적**: 트랜잭션 단위로 Connection 관리
- **장점**: Connection 재사용 효율적
- **단점**: Connection이 Supabase 레벨에서 관리되어 애플리케이션 제어 어려움

### Session Pooler (port 6543)
- **목적**: 세션 단위로 Connection 관리
- **장점**: Prepared Statement 사용 가능
- **단점**: Connection이 세션 종료까지 유지되어 leak 위험 높음

### Direct Connection (포트 확인 필요)
- **목적**: Pooler 우회, 직접 연결
- **장점**: 완전한 제어 가능
- **단점**: Connection 수 제한에 직접 영향

## 🔧 즉시 적용 가능한 해결책

### Option A: PostgreSQL 설정 적용 (권장)

```sql
-- Supabase SQL Editor에서 실행
SET idle_in_transaction_session_timeout = '5min';
```

그리고 애플리케이션 시작 시 자동으로 설정하도록 코드 추가.

### Option B: HikariCP 설정 최적화

```properties
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.idle-timeout=180000
spring.datasource.hikari.max-lifetime=300000
spring.datasource.hikari.keepalive-time=30000
```

### Option C: Direct Connection 사용

Pooler를 우회하고 직접 연결 (Supabase 문서에서 Direct Connection 포트 확인 필요)

## 📝 체크리스트

- [ ] Supabase에서 `idle_in_transaction_session_timeout` 설정 가능 여부 확인
- [ ] Supabase 지원팀에 데이터베이스 레벨 설정 문의
- [ ] HikariCP 설정 최적화 적용
- [ ] 애플리케이션 시작 시 PostgreSQL 설정 자동 적용
- [ ] Connection 모니터링 및 자동 정리 로직 추가
- [ ] Direct Connection 사용 검토 (필요시)

## 🔗 참고 자료

- [Supabase Connection Pooling](https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooler)
- [PostgreSQL idle_in_transaction_session_timeout](https://www.postgresql.org/docs/current/runtime-config-client.html#GUC-IDLE-IN-TRANSACTION-SESSION-TIMEOUT)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [pgbouncer Transaction Pooling](https://www.pgbouncer.org/features.html#transaction-pooling)

