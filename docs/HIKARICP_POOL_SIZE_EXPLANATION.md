# HikariCP Pool Size 동작 방식

## maximum-pool-size의 의미

### 기본 개념

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
```

**`maximum-pool-size=10`의 의미:**
- **최대 10개까지만** Connection을 생성할 수 있음
- **10개를 초과하여 생성할 수 없음**
- 하지만 **항상 10개가 유지되는 것은 아님**

## 실제 동작 방식

### 1. Connection 수의 변동 범위

```
최소: minimum-idle = 2개
최대: maximum-pool-size = 10개
실제: 2 ~ 10개 사이에서 변동
```

### 2. Connection 생성 조건

**Connection이 생성되는 경우:**
1. **초기 시작 시**: `minimum-idle=2`이므로 최소 2개 생성
2. **요청 증가 시**: Connection 요청이 많아지면 최대 10개까지 생성
3. **Connection 부족 시**: Active Connection이 10개에 도달하면
   - 새로운 Connection 요청은 **대기** (connection-timeout 내)
   - 또는 **예외 발생** (connection-timeout 초과)

**Connection이 생성되지 않는 경우:**
- 이미 10개가 생성되어 있으면 **더 이상 생성할 수 없음**
- 새로운 Connection 요청은 **대기**하거나 **예외 발생**

### 3. Connection 제거 조건

**Connection이 제거되는 경우:**
1. **idle-timeout 도달**: Idle Connection이 3분 이상 유지되면 제거
   - 단, `minimum-idle=2` 개수는 유지
2. **max-lifetime 도달**: Connection이 생성된 후 5분 이상 지나면 교체
3. **Pool 크기 조정**: 요청이 감소하면 불필요한 Connection 제거

**Connection이 제거되지 않는 경우:**
- `minimum-idle=2` 개수는 항상 유지
- Active Connection은 제거되지 않음

## 실제 시나리오

### 시나리오 1: 애플리케이션 시작 시

```
시간: 00:00:00
상태: Connection 2개 생성 (minimum-idle)
로그: "Connection added (initial pool fill)"
```

### 시나리오 2: 요청 증가 시

```
시간: 00:01:00
요청: 동시에 5개의 트랜잭션 요청
상태: Connection 5개로 증가 (2 → 5)
로그: "Connection added (demand)"
```

### 시나리오 3: 최대 Connection 도달

```
시간: 00:02:00
요청: 동시에 15개의 트랜잭션 요청
상태: Connection 10개로 증가 (5 → 10)
      5개는 대기 또는 예외 발생
로그: "Connection added (demand)"
      "Threads Awaiting Connection: 5"
```

### 시나리오 4: 요청 감소 시

```
시간: 00:10:00
요청: 트랜잭션 요청이 없음
상태: Idle Connection이 3분 이상 유지
      Connection 10개 → 2개로 감소 (minimum-idle 유지)
로그: "Connection evicted (idle-timeout)"
```

### 시나리오 5: 정상 운영 시

```
시간: 00:15:00
요청: 평상시 트랜잭션 요청
상태: Connection 2 ~ 5개 사이에서 변동
      (요청에 따라 동적으로 조정)
```

## 중요한 포인트

### 1. maximum-pool-size는 상한선

- **10개를 초과하여 생성할 수 없음**
- 10개에 도달하면 새로운 Connection 요청은 대기하거나 예외 발생
- 하지만 **항상 10개가 유지되는 것은 아님**

### 2. minimum-idle은 하한선

- **최소 2개는 항상 유지**
- 요청이 없어도 2개는 유지됨
- Connection 재사용을 위해 필요

### 3. 실제 Connection 수는 동적

```
실제 Connection 수 = min(maximum-pool-size, max(minimum-idle, 요청에 필요한 Connection 수))
```

**예시:**
- 요청이 없을 때: 2개 (minimum-idle)
- 요청이 적을 때: 3 ~ 5개
- 요청이 많을 때: 8 ~ 10개
- 요청이 매우 많을 때: 10개 (maximum-pool-size)

## 현재 설정 요약

```properties
minimum-idle=2        # 최소 2개 유지
maximum-pool-size=10  # 최대 10개까지만 생성 가능
idle-timeout=3분      # Idle 3분 이상 시 제거 (단, minimum-idle은 유지)
max-lifetime=5분      # 생성 후 5분 이상 시 교체
```

## 결론

**`maximum-pool-size=10`의 의미:**
- ✅ **최대 10개까지만** Connection을 생성할 수 있음
- ✅ **10개를 초과하여 생성할 수 없음**
- ❌ **항상 10개가 유지되는 것은 아님**
- ✅ **2 ~ 10개 사이에서 동적으로 변동**

**실제 운영:**
- 요청이 없을 때: 2개 (minimum-idle)
- 요청이 적을 때: 3 ~ 5개
- 요청이 많을 때: 8 ~ 10개
- 요청이 매우 많을 때: 10개 (maximum-pool-size)

**10개를 초과하는 경우:**
- ❌ **생성할 수 없음**
- 새로운 Connection 요청은 **대기** (connection-timeout=30초 내)
- 또는 **예외 발생** (connection-timeout 초과)

