# Connection 생성 원인 추적 가이드

## 개요
Idle Connection이 생성되는 정확한 원인을 추적하는 방법을 설명합니다.

## 1. 로그 분석 방법

### 1.1 HikariCP Connection 생성 로그
HikariCP는 Connection이 생성될 때마다 상세 로그를 출력합니다.

**로그 패턴:**
```
DEBUG com.zaxxer.hikari.pool.HikariPool - Added connection ...
DEBUG com.zaxxer.hikari.pool.HikariPool - Connection added to pool ...
```

**확인 사항:**
- Connection 생성 시점 (타임스탬프)
- Connection 생성 이유 (pool이 부족해서, validation 실패로 인한 재생성 등)

### 1.2 트랜잭션 시작/종료 로그
Spring Transaction 로그에서 어떤 메서드가 트랜잭션을 시작했는지 확인:

**로그 패턴:**
```
DEBUG o.s.orm.jpa.JpaTransactionManager - Creating new transaction ...
DEBUG o.s.orm.jpa.JpaTransactionManager - Initiating transaction commit ...
```

**확인 사항:**
- 트랜잭션을 시작한 메서드 (`@Transactional` 메서드)
- 트랜잭션 시작 시점
- 트랜잭션 종료 여부 (commit/rollback)

### 1.3 Connection Leak 감지 로그
HikariCP의 `leak-detection-threshold`가 설정되어 있으면 Connection leak을 감지합니다.

**로그 패턴:**
```
WARN com.zaxxer.hikari.pool.ProxyConnection - Connection leak detection triggered ...
```

**확인 사항:**
- Connection을 점유한 스레드 이름
- Connection을 획득한 시점의 스택 트레이스

## 2. 실시간 모니터링

### 2.1 HikariConnectionPoolMonitor
애플리케이션 로그에서 다음 정보를 확인:

```
🔍 Connection 생성 감지: 10개
Thread: http-nio-8080-exec-1
호출 스택:
  → com.spectrum.workfolio.services.turnovers.TurnOverService.listTurnOverDetailsResult(TurnOverService.kt:123)
  → com.spectrum.workfolio.controllers.TurnOverController.listTurnOvers(TurnOverController.kt:45)
  ...
```

### 2.2 PostgreSQL pg_stat_activity
Supabase에서 다음 쿼리로 Connection 상태 확인:

```sql
SELECT 
  pid as connection_id,
  application_name,
  state,
  query,
  query_start,
  backend_start,
  state_change,
  NOW() - backend_start as connection_duration,
  NOW() - state_change as idle_duration
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND state = 'idle'
ORDER BY backend_start;
```

**확인 사항:**
- `backend_start`: Connection 생성 시점
- `idle_duration`: Idle 상태로 유지된 시간
- `query`: 마지막 실행된 쿼리

## 3. Connection 생성 원인 분석

### 3.1 일반적인 원인

#### 1) 동시 요청으로 인한 Connection 생성
- 여러 요청이 동시에 들어와서 Connection이 부족하면 새로 생성
- **증상**: 짧은 시간에 여러 Connection 생성
- **해결**: `maximum-pool-size` 조정 또는 요청 처리 최적화

#### 2) 트랜잭션 타임아웃으로 인한 Connection 미반환
- 트랜잭션이 타임아웃되어도 Connection이 즉시 반환되지 않을 수 있음
- **증상**: Idle Connection이 오래 유지됨
- **해결**: 전역 트랜잭션 타임아웃 설정 확인

#### 3) 외부 API 호출로 인한 긴 트랜잭션
- 트랜잭션 안에서 외부 API를 호출하면 Connection이 오래 점유됨
- **증상**: Active Connection이 오래 유지됨
- **해결**: 외부 API 호출을 트랜잭션 밖으로 이동

#### 4) Connection Leak
- 트랜잭션이 종료되지 않아 Connection이 반환되지 않음
- **증상**: Connection이 계속 증가하고 반환되지 않음
- **해결**: `leak-detection-threshold` 로그 확인 및 코드 수정

### 3.2 특정 시나리오 분석

#### 시나리오 1: 10개 Connection이 한 번에 생성됨
**가능한 원인:**
- 대량 데이터 조회/생성 작업
- N+1 쿼리 문제로 인한 다수 쿼리 실행
- 동시 요청 처리

**확인 방법:**
```bash
# 로그에서 Connection 생성 시점 확인
grep "Connection 생성 감지" application.log | tail -20

# 트랜잭션 로그에서 해당 시점의 메서드 확인
grep "Creating new transaction" application.log | tail -20
```

#### 시나리오 2: Connection이 생성되지만 반환되지 않음
**가능한 원인:**
- 트랜잭션이 완료되지 않음 (예외 발생, 타임아웃)
- Connection leak

**확인 방법:**
```sql
-- Supabase에서 Connection 상태 확인
SELECT 
  pid,
  state,
  query,
  NOW() - backend_start as connection_duration,
  NOW() - state_change as idle_duration
FROM pg_stat_activity
WHERE application_name = 'PostgreSQL JDBC Driver'
  AND state = 'idle'
ORDER BY backend_start;
```

## 4. 디버깅 명령어

### 4.1 로그 필터링
```bash
# Connection 생성 로그만 확인
grep "Added connection\|Connection added" application.log

# 트랜잭션 시작 로그만 확인
grep "Creating new transaction\|Initiating transaction" application.log

# Connection leak 감지 로그만 확인
grep "Connection leak detection" application.log
```

### 4.2 특정 시간대 분석
```bash
# 특정 시간대의 Connection 생성 로그 확인
grep "2025-11-28 07:41" application.log | grep "Connection 생성 감지"
```

### 4.3 스레드별 Connection 사용 분석
```bash
# 특정 스레드가 Connection을 사용한 이력 확인
grep "http-nio-8080-exec" application.log | grep -E "Connection|transaction"
```

## 5. 예방 방법

### 5.1 트랜잭션 범위 최소화
- 필요한 경우에만 `@Transactional` 사용
- 읽기 전용 작업은 `@Transactional(readOnly = true)` 사용

### 5.2 외부 API 호출 분리
- 트랜잭션 안에서 외부 API 호출 금지
- 외부 API 호출 후 트랜잭션 시작

### 5.3 Connection Pool 모니터링
- `HikariConnectionPoolMonitor`로 주기적 모니터링
- Connection 증가 추세 확인

### 5.4 적절한 타임아웃 설정
- 전역 트랜잭션 타임아웃 설정 (현재: 30초)
- 긴 작업은 개별 타임아웃 설정

## 6. 문제 해결 체크리스트

- [ ] HikariCP 로그에서 Connection 생성 시점 확인
- [ ] 트랜잭션 로그에서 해당 시점의 메서드 확인
- [ ] `pg_stat_activity`에서 Connection 상태 확인
- [ ] Connection leak 감지 로그 확인
- [ ] 스레드 덤프로 Connection 점유 스레드 확인
- [ ] 트랜잭션 타임아웃 발생 여부 확인
- [ ] 외부 API 호출이 트랜잭션 안에 있는지 확인

