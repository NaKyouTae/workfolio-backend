# Connection 고정 Pool 전략

## 전략 개요

Connection 제거를 비활성화하고 항상 10개를 고정으로 유지하는 전략입니다.

### 설정

```properties
spring.datasource.hikari.minimum-idle=10        # maximum-pool-size와 동일
spring.datasource.hikari.maximum-pool-size=10  # 최대 10개
spring.datasource.hikari.idle-timeout=0        # 0 = 비활성화 (Connection 제거 안 함)
spring.datasource.hikari.max-lifetime=86400000 # 24시간 (교체 최소화)
spring.datasource.hikari.keepalive-time=30000  # 30초 (죽은 Connection 감지)
```

## 동작 방식

### 1. Connection 생성

**애플리케이션 시작 시:**
- `minimum-idle=10`이므로 **즉시 10개 생성**
- 이후 **새로 생성하지 않음**

**요청 증가 시:**
- 이미 10개가 있으므로 **새로 생성하지 않음**
- 기존 Connection을 **재사용**

### 2. Connection 제거

**제거되지 않음:**
- `idle-timeout=0`이므로 Connection이 제거되지 않음
- `max-lifetime=24시간`이므로 교체도 거의 없음
- **항상 10개 유지**

**제거되는 경우:**
- `keepalive-time=30초`마다 `SELECT 1` 쿼리 실행
- 쿼리 실패 시 (죽은 Connection) → 제거 후 새 Connection 생성
- 이 경우에만 새로 생성됨

## 장점

### 1. Connection 누적 문제 해결

**문제:**
- HikariCP가 Connection을 제거했지만 PostgreSQL에서는 닫히지 않음
- Connection이 계속 누적됨

**해결:**
- Connection이 제거되지 않으므로 HikariCP와 PostgreSQL Connection 수가 일치
- 누적 문제 해결

### 2. Connection 재사용 극대화

**이전:**
- Connection이 제거되어 새로 생성해야 함
- Connection 생성 오버헤드 발생

**현재:**
- Connection이 제거되지 않아 항상 재사용
- Connection 생성 오버헤드 없음

### 3. 예측 가능한 동작

**이전:**
- Connection 수가 2 ~ 10개 사이에서 변동
- 예측하기 어려움

**현재:**
- Connection 수가 항상 10개로 고정
- 예측 가능하고 안정적

## 단점 및 대응

### 1. 오래된 Connection 문제

**우려:**
- 오래된 Connection이 문제가 될 수 있음

**대응:**
- `keepalive-time=30초`로 죽은 Connection 감지
- 문제가 있는 Connection은 자동으로 교체됨

### 2. 리소스 사용량 고정

**우려:**
- 요청이 적을 때도 10개를 유지하여 리소스 낭비

**대응:**
- 10개는 적절한 수준 (Supabase Pool Size 40개 중 10개)
- Connection 재사용으로 성능 향상

## 실제 동작

### 시나리오 1: 애플리케이션 시작

```
시간: 00:00:00
동작: Connection 10개 즉시 생성
상태: Connection 10개 (고정)
```

### 시나리오 2: 요청 처리

```
시간: 00:01:00
요청: 동시에 5개의 트랜잭션
동작: 기존 Connection 5개 재사용
상태: Connection 10개 (고정, Active 5개, Idle 5개)
```

### 시나리오 3: 요청 증가

```
시간: 00:02:00
요청: 동시에 15개의 트랜잭션
동작: 기존 Connection 10개 재사용
      5개는 대기 (connection-timeout=30초)
상태: Connection 10개 (고정, Active 10개, Idle 0개)
```

### 시나리오 4: 요청 감소

```
시간: 00:10:00
요청: 트랜잭션 요청이 없음
동작: Connection 제거 안 함 (idle-timeout=0)
상태: Connection 10개 (고정, Active 0개, Idle 10개)
```

### 시나리오 5: 죽은 Connection 감지

```
시간: 00:15:00
동작: keepalive-time=30초마다 SELECT 1 쿼리 실행
      쿼리 실패 시 → Connection 제거 후 새 Connection 생성
상태: Connection 10개 (고정, 죽은 Connection은 교체)
```

## 비교

### 이전 설정 (동적 Pool)

```properties
minimum-idle=2
maximum-pool-size=10
idle-timeout=3분
max-lifetime=5분
```

**동작:**
- Connection 수: 2 ~ 10개 사이에서 변동
- Connection 제거: 3분 이상 idle 시 제거
- Connection 교체: 5분 이상 사용 시 교체
- 문제: HikariCP와 PostgreSQL Connection 수 불일치

### 현재 설정 (고정 Pool)

```properties
minimum-idle=10
maximum-pool-size=10
idle-timeout=0 (비활성화)
max-lifetime=24시간
```

**동작:**
- Connection 수: 항상 10개 (고정)
- Connection 제거: 안 함 (idle-timeout=0)
- Connection 교체: 거의 안 함 (max-lifetime=24시간)
- 장점: HikariCP와 PostgreSQL Connection 수 일치

## 모니터링

### Connection 수 확인

**HikariCP:**
```
Active Connections: X / 10
Idle Connections: Y
Total Connections: 10 / 10 (고정)
```

**PostgreSQL:**
```sql
SELECT COUNT(*)
FROM pg_stat_activity
WHERE application_name LIKE 'workfolio-server-%'
  AND state = 'idle';
-- 예상 결과: 10개 (HikariCP와 일치)
```

## 결론

**Connection 고정 Pool 전략의 장점:**
1. ✅ Connection 누적 문제 해결 (HikariCP와 PostgreSQL Connection 수 일치)
2. ✅ Connection 재사용 극대화 (새로 생성하지 않음)
3. ✅ 예측 가능한 동작 (항상 10개 고정)
4. ✅ 성능 향상 (Connection 생성 오버헤드 없음)

**주의사항:**
- `keepalive-time=30초`로 죽은 Connection 감지 필수
- 오래된 Connection 문제는 keepalive로 해결
- 리소스 사용량이 고정되지만 10개는 적절한 수준

**권장:**
- Supabase Transaction Pooler를 사용하는 경우 이 전략이 가장 효과적
- Connection 누적 문제를 근본적으로 해결

