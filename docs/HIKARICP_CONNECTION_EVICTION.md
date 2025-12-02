# HikariCP Connection 제거 기준 및 주체

## Connection 제거 주체

**HikariCP의 Housekeeper 스레드**가 Connection을 제거하는 주체입니다.

- **스레드 이름**: `HikariPool-{poolName} housekeeper`
- **실행 주기**: 약 30초마다 실행 (HikariCP 내부 설정)
- **역할**: 
  - Idle Connection 정리
  - 오래된 Connection 교체
  - 죽은 Connection 제거
  - Pool 크기 조정

## Connection 제거 기준

### 1. idle-timeout (유휴 시간 초과)

**현재 설정:**
```properties
spring.datasource.hikari.idle-timeout=180000  # 3분
spring.datasource.hikari.minimum-idle=2
```

**제거 조건:**
- Connection이 **idle 상태로 3분 이상** 유지되면 제거
- **단, `minimum-idle` 개수는 유지됨**
  - 예: `minimum-idle=2`, `idle-timeout=3분`인 경우
  - Idle Connection이 5개 있으면 → 3개 제거 (최소 2개 유지)
  - Idle Connection이 2개 있으면 → 제거 안 함 (최소 2개 유지)

**중요:**
- `idle-timeout`은 `minimum-idle`보다 **작으면 작동하지 않음**
- `minimum-idle=2`, `idle-timeout=1분` → 작동 안 함
- `minimum-idle=2`, `idle-timeout=3분` → 작동함

**로그 예시:**
```
HikariPool-1 - Pool stats (total=10, active=0, idle=10, waiting=0)
HikariPool-1 - Connection evicted (idle-timeout)
```

### 2. max-lifetime (최대 수명 초과)

**현재 설정:**
```properties
spring.datasource.hikari.max-lifetime=300000  # 5분
```

**제거 조건:**
- Connection이 **생성된 후 5분 이상** 지나면 제거
- Connection이 사용 중이어도 제거됨 (다음 사용 시 교체)
- Connection이 idle 상태여도 제거됨

**목적:**
- 오래된 Connection을 교체하여 데이터베이스 연결 문제 방지
- 네트워크 타임아웃, 방화벽 설정 등으로 인한 죽은 Connection 방지

**로그 예시:**
```
HikariPool-1 - Connection evicted (max-lifetime)
HikariPool-1 - Connection added (replacement)
```

### 3. keepalive-time (Connection 유효성 검사)

**현재 설정:**
```properties
spring.datasource.hikari.keepalive-time=30000  # 30초
spring.datasource.hikari.connection-test-query=SELECT 1
```

**제거 조건:**
- 30초마다 Connection에 `SELECT 1` 쿼리 실행
- 쿼리 실패 시 Connection이 죽은 것으로 판단하여 제거
- 다음 사용 시 새로운 Connection으로 교체

**목적:**
- 죽은 Connection을 빠르게 감지
- 데이터베이스 재시작, 네트워크 문제 등으로 인한 Connection 문제 방지

**로그 예시:**
```
HikariPool-1 - Connection validation failed
HikariPool-1 - Connection evicted (keepalive failed)
```

### 4. Connection Validation 실패

**현재 설정:**
```properties
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=5000
```

**제거 조건:**
- Connection을 사용하기 전에 `SELECT 1` 쿼리 실행
- 5초 내에 응답이 없으면 Connection이 유효하지 않다고 판단
- 유효하지 않은 Connection은 제거하고 새로운 Connection 생성

**목적:**
- 사용 직전에 Connection 유효성 확인
- 죽은 Connection 사용 방지

### 5. Pool 크기 조정

**현재 설정:**
```properties
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.maximum-pool-size=10
```

**제거 조건:**
- Active Connection이 많아서 Pool이 가득 찬 경우
- 새로운 Connection 요청이 있을 때
- Idle Connection을 제거하고 새로운 Connection 생성

**동작:**
- `maximum-pool-size=10`에 도달하면
- 새로운 Connection 요청 시 → Idle Connection 제거 후 새 Connection 생성
- 또는 Connection 요청 대기

## 현재 설정 요약

```properties
# 최소 Connection 수 (항상 유지)
spring.datasource.hikari.minimum-idle=2

# 최대 Connection 수
spring.datasource.hikari.maximum-pool-size=10

# Idle Connection 제거 기준 (3분)
spring.datasource.hikari.idle-timeout=180000

# Connection 최대 수명 (5분)
spring.datasource.hikari.max-lifetime=300000

# Connection 유효성 검사 주기 (30초)
spring.datasource.hikari.keepalive-time=30000
```

## 실제 동작 예시

### 시나리오 1: 애플리케이션 시작 시

1. **초기 Connection 생성**
   - `minimum-idle=2`이므로 2개 Connection 생성
   - 로그: `Connection added (initial pool fill)`

2. **요청 증가 시**
   - Connection 요청이 많아지면 최대 10개까지 생성
   - 로그: `Connection added (demand)`

3. **요청 감소 시**
   - Idle Connection이 3분 이상 유지되면 제거
   - 단, 최소 2개는 유지
   - 로그: `Connection evicted (idle-timeout)`

### 시나리오 2: Connection이 5분 이상 사용된 경우

1. **max-lifetime 도달**
   - Connection이 생성된 후 5분 지나면 제거 대상
   - 다음 사용 시 새로운 Connection으로 교체
   - 로그: `Connection evicted (max-lifetime)`

### 시나리오 3: 데이터베이스 재시작

1. **keepalive 실패**
   - 30초마다 `SELECT 1` 쿼리 실행
   - 데이터베이스가 재시작되면 쿼리 실패
   - Connection 제거
   - 로그: `Connection evicted (keepalive failed)`

## 모니터링

### HikariCP 로그 확인

```properties
logging.level.com.zaxxer.hikari=DEBUG
```

**주요 로그 메시지:**
- `Connection added` - Connection 생성
- `Connection evicted` - Connection 제거
- `Pool stats` - Pool 상태 (30초마다)
- `Fill pool skipped` - Pool이 충분한 경우

### 애플리케이션 로그 확인

`HikariConnectionPoolMonitor`가 30초마다 Pool 상태를 로깅합니다:

```
=== HikariCP Connection Pool Status ===
Active Connections: 0 / 10
Idle Connections: 10
Total Connections: 10 / 10
```

## 주의사항

### 1. minimum-idle과 idle-timeout의 관계

- `idle-timeout`은 `minimum-idle`보다 **작으면 작동하지 않음**
- 예: `minimum-idle=2`, `idle-timeout=1분` → 작동 안 함
- 예: `minimum-idle=2`, `idle-timeout=3분` → 작동함

### 2. max-lifetime과 idle-timeout의 관계

- `max-lifetime`은 Connection의 **절대 수명**
- `idle-timeout`은 Connection의 **유휴 시간**
- 둘 다 적용되므로, 더 짧은 시간에 도달하는 기준이 먼저 적용됨

### 3. Connection이 10개로 고정되는 이유

- `maximum-pool-size=10`이므로 최대 10개
- `minimum-idle=2`이므로 최소 2개 유지
- 실제로는 요청에 따라 2~10개 사이에서 변동

## 결론

**Connection 제거 주체:**
- HikariCP의 **Housekeeper 스레드** (약 30초마다 실행)

**Connection 제거 기준:**
1. **idle-timeout**: Idle 상태로 3분 이상 (단, minimum-idle 개수는 유지)
2. **max-lifetime**: 생성 후 5분 이상
3. **keepalive 실패**: Connection 유효성 검사 실패
4. **Validation 실패**: 사용 전 Connection 검증 실패
5. **Pool 크기 조정**: maximum-pool-size 초과 시

**현재 설정:**
- 최소 2개, 최대 10개 Connection 유지
- Idle Connection은 3분 이상 유지 시 제거 (단, 최소 2개는 유지)
- Connection은 5분 이상 사용 시 교체

