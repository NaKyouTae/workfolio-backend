# HikariCP Housekeeper vs 스케줄러 비교

## Housekeeper의 역할

### 동작 방식
- **스레드**: `HikariPool-{poolName} housekeeper`
- **실행 주기**: 약 30초마다 실행 (HikariCP 내부 설정)
- **작업 레벨**: **HikariCP Pool 레벨**

### 수행 작업
1. **Idle Connection 정리**
   - `idle-timeout=3분` 도달 시 Connection 제거
   - `Connection.close()` 호출
   - **단, PostgreSQL 레벨에서는 닫히지 않을 수 있음**

2. **오래된 Connection 교체**
   - `max-lifetime=5분` 도달 시 Connection 교체
   - `Connection.close()` 호출 후 새 Connection 생성

3. **죽은 Connection 제거**
   - `keepalive-time=30초`마다 `SELECT 1` 쿼리 실행
   - 실패 시 Connection 제거

4. **Pool 크기 조정**
   - `minimum-idle` 유지
   - `maximum-pool-size` 초과 시 Connection 제거

### 한계
- **HikariCP Pool 레벨에서만 작동**
- `Connection.close()`를 호출하지만, Supabase Transaction Pooler를 사용하면
  PostgreSQL 레벨에서는 Connection이 실제로 닫히지 않을 수 있음
- PostgreSQL 레벨의 Connection은 제거하지 않음

## 스케줄러의 역할

### 동작 방식
- **메서드**: `HikariConnectionPoolMonitor.evictIdleConnections()`
- **실행 주기**: 2분마다 실행
- **작업 레벨**: **PostgreSQL 레벨**

### 수행 작업
1. **Connection 수 비교**
   - HikariCP Connection 수와 PostgreSQL Connection 수 비교
   - 불일치 감지 시 경고 로그 출력

2. **오래된 Connection 종료**
   - 4분 이상 idle 상태인 Connection을 PostgreSQL에서 직접 종료
   - `pg_terminate_backend()` 함수 사용
   - **실제로 PostgreSQL 레벨에서 Connection 종료**

### 목적
- Housekeeper가 제거했다고 판단한 Connection이 PostgreSQL에서
  여전히 idle 상태로 유지되는 경우를 처리
- HikariCP Connection 수와 PostgreSQL Connection 수 동기화

## 겹치지 않는 이유

### 1. 작업 레벨이 다름
- **Housekeeper**: HikariCP Pool 레벨 (애플리케이션 레벨)
- **스케줄러**: PostgreSQL 레벨 (데이터베이스 레벨)

### 2. 실행 주기가 다름
- **Housekeeper**: 약 30초마다 실행
- **스케줄러**: 2분마다 실행 (Housekeeper보다 훨씬 느림)

### 3. 처리 대상이 다름
- **Housekeeper**: HikariCP Pool의 Connection
- **스케줄러**: PostgreSQL에서 idle 상태로 유지되는 Connection
  (Housekeeper가 제거했다고 판단했지만 실제로 닫히지 않은 Connection)

### 4. 처리 방법이 다름
- **Housekeeper**: `Connection.close()` 호출
- **스케줄러**: `pg_terminate_backend()` 호출 (PostgreSQL 레벨에서 강제 종료)

## 실제 동작 시나리오

### 시나리오 1: 정상적인 경우

1. **Housekeeper 실행 (30초마다)**
   - Idle Connection이 3분 이상 유지됨
   - `Connection.close()` 호출
   - Connection이 실제로 닫힘 ✅
   - PostgreSQL에서도 Connection이 종료됨

2. **스케줄러 실행 (2분마다)**
   - Connection 수 비교: 일치 ✅
   - 종료할 Connection 없음
   - 로그: "종료할 Connection이 없습니다."

### 시나리오 2: 문제가 있는 경우

1. **Housekeeper 실행 (30초마다)**
   - Idle Connection이 3분 이상 유지됨
   - `Connection.close()` 호출
   - **하지만 Supabase Transaction Pooler 때문에 PostgreSQL 레벨에서는 닫히지 않음** ❌
   - HikariCP Pool에서는 Connection이 제거됨

2. **스케줄러 실행 (2분마다)**
   - Connection 수 비교: 불일치 감지!
     - HikariCP: 10개
     - PostgreSQL: 20개
   - 4분 이상 idle 상태인 Connection 종료
   - `pg_terminate_backend()` 호출
   - PostgreSQL에서 Connection이 실제로 종료됨 ✅

## 최적화된 동작

### 현재 구현

```kotlin
@Scheduled(fixedRate = 120000, initialDelay = 60000) // 2분마다, 시작 후 1분 대기
fun evictIdleConnections() {
    // PostgreSQL Connection 수가 HikariCP보다 많은 경우만 정리
    if (postgresConnectionCount > total) {
        // 4분 이상 idle 상태인 Connection만 종료
        // (Housekeeper가 방금 제거한 Connection은 제외)
    }
}
```

### 장점
1. **Housekeeper와 겹치지 않음**
   - Housekeeper가 처리한 Connection은 제외
   - 실제로 닫히지 않은 Connection만 처리

2. **효율적**
   - 불일치가 있을 때만 실행
   - 4분 이상 idle 상태인 Connection만 종료 (idle-timeout=3분보다 약간 긴 시간)

3. **안전함**
   - Housekeeper가 방금 제거한 Connection은 제외
   - 실제로 문제가 있는 Connection만 종료

## 결론

**Housekeeper와 스케줄러는 겹치지 않습니다:**

1. **작업 레벨이 다름**: Housekeeper는 HikariCP Pool 레벨, 스케줄러는 PostgreSQL 레벨
2. **실행 주기가 다름**: Housekeeper는 30초마다, 스케줄러는 2분마다
3. **처리 대상이 다름**: Housekeeper는 Pool의 Connection, 스케줄러는 PostgreSQL의 idle Connection
4. **처리 방법이 다름**: Housekeeper는 `Connection.close()`, 스케줄러는 `pg_terminate_backend()`

**스케줄러는 Housekeeper의 보완 역할:**
- Housekeeper가 제거했다고 판단한 Connection이 PostgreSQL에서
  여전히 idle 상태로 유지되는 경우를 처리
- HikariCP Connection 수와 PostgreSQL Connection 수를 동기화

