# Connection 재사용 문제 분석

## 문제 상황

9개의 새로운 Connection이 생성되었는데, 기존 Connection을 재사용하지 않고 계속 새로 생성되는 문제가 발생했습니다.

### 관찰된 패턴
- **00:44:17~00:44:18**: 6개 Connection 생성 (120302~120307)
- **00:56:02~00:56:03**: 9개 Connection 생성 (120866~120874)
- 모든 Connection이 `idle` 상태로 12분 이상 유지됨
- `application_name`이 모두 동일 (`workfolio-server-814c4072`)

## Connection이 재사용되지 않는 주요 원인

### 1. Connection Validation 실패 ⚠️ (가장 가능성 높음)

**원인:**
- HikariCP는 Connection을 사용하기 전에 유효성을 검사합니다
- `connection-test-query=SELECT 1`이 설정되어 있지만, Supabase Transaction Pooler를 사용하면 Connection이 이미 닫혔을 수 있습니다
- Transaction Pooler는 트랜잭션 종료 시 Connection을 즉시 반환하므로, HikariCP가 반환된 Connection을 재사용하려고 할 때 이미 닫혀있을 수 있습니다

**확인 방법:**
```sql
-- Connection이 실제로 닫혔는지 확인
SELECT 
  pid,
  state,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration
FROM pg_stat_activity
WHERE application_name = 'workfolio-server-814c4072'
ORDER BY backend_start;
```

**해결책:**
```properties
# Connection validation을 더 엄격하게 설정
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=5000

# 또는 validation을 비활성화하고 Connection 재사용 우선
# (주의: 죽은 Connection을 사용할 위험이 있음)
# spring.datasource.hikari.connection-test-query=
```

### 2. max-lifetime 도달로 인한 Connection 교체

**현재 설정:**
```properties
spring.datasource.hikari.max-lifetime=300000  # 5분
```

**문제:**
- Connection이 5분 이상 사용되면 강제로 교체됩니다
- 하지만 사용자가 제공한 데이터를 보면 Connection이 12분 이상 유지되고 있으므로, 이는 PostgreSQL 레벨에서의 Connection이고 HikariCP 레벨에서는 이미 교체되었을 수 있습니다

**확인 방법:**
- HikariCP 로그에서 "Connection evicted" 메시지 확인
- `HikariConnectionPoolMonitor` 로그에서 Connection 감소 확인

### 3. 동시 요청으로 인한 Connection 부족

**현재 설정:**
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=0
```

**문제:**
- `minimum-idle=0`이므로 Connection이 필요할 때만 생성됩니다
- 여러 요청이 동시에 들어오면 Connection이 부족하여 새로 생성됩니다
- 하지만 사용자가 제공한 데이터를 보면 Connection이 12분 이상 idle 상태로 유지되고 있으므로, 이는 Connection이 제대로 반환되지 않았음을 의미합니다

**해결책:**
```properties
# 최소 Connection 수를 설정하여 Connection 재사용 촉진
spring.datasource.hikari.minimum-idle=2  # 최소 2개 유지
```

### 4. 트랜잭션이 완료되지 않아 Connection이 반환되지 않음 ⚠️

**가능한 원인:**
1. **외부 API 호출이 트랜잭션 안에 있음**
   - `AttachmentCommandService.createAttachment`에서 Supabase Storage 업로드가 트랜잭션 안에서 실행됨
   - 외부 API 호출이 오래 걸리면 Connection이 오래 점유됨

2. **트랜잭션 타임아웃이 충분하지 않음**
   - 현재 전역 타임아웃: 30초
   - 일부 메서드는 60초 타임아웃 설정

3. **예외 발생 시 롤백이 제대로 되지 않음**

**확인 방법:**
```sql
-- "idle in transaction" 상태인 Connection 확인
SELECT 
  pid,
  state,
  NOW() - query_start as idle_duration,
  query
FROM pg_stat_activity
WHERE application_name = 'workfolio-server-814c4072'
  AND state = 'idle in transaction';
```

**해결책:**
- 외부 API 호출을 트랜잭션 밖으로 분리
- 트랜잭션 타임아웃을 적절히 설정
- 예외 처리 강화

### 5. Supabase Transaction Pooler의 Connection 반환 문제 ⚠️

**문제:**
- Transaction Pooler는 트랜잭션 종료 시 Connection을 즉시 반환해야 하지만
- 실제로는 Connection이 PostgreSQL 레벨에서 idle 상태로 유지됨
- HikariCP는 Connection이 반환된 것으로 인식하지만, PostgreSQL에서는 여전히 Connection이 살아있음
- HikariCP가 새로운 Connection을 요청하면, Supabase가 새로운 Connection을 생성함

**확인 방법:**
- HikariCP 로그에서 Connection 반환 확인
- PostgreSQL에서 Connection 상태 확인

**해결책:**
- Direct Connection 사용 (Pooler 우회)
- 또는 Supabase 지원팀에 문의

## 코드 구조 분석

### 문제가 될 수 있는 패턴

#### 1. 외부 API 호출이 트랜잭션 안에 있음

```kotlin
@Transactional  // 전역 타임아웃(30초) 적용
fun createAttachment(dto: AttachmentCreateDto): Attachment {
    val savedAttachment = attachmentRepository.save(attachment)
    
    // ⚠️ 외부 API 호출이 트랜잭션 안에서 실행됨
    // Connection이 오래 점유될 수 있음
    val uploadedFileUrl = if (dto.fileData != null) {
        fileUploadService.uploadFileToStorage(...)  // 외부 API 호출
    } else {
        dto.fileUrl ?: ""
    }
    
    savedAttachment.changeFileUrl(uploadedFileUrl)
    return savedAttachment
}
```

**해결책:**
```kotlin
@Transactional
fun createAttachment(dto: AttachmentCreateDto): Attachment {
    val savedAttachment = attachmentRepository.save(attachment)
    // 트랜잭션 커밋
    return savedAttachment
}

// 별도 메서드로 분리
fun uploadFileAfterCommit(attachmentId: String, fileData: ByteString) {
    // 트랜잭션 없이 실행
    fileUploadService.uploadFileToStorage(...)
}
```

#### 2. 중첩 트랜잭션

여러 `@Transactional` 메서드가 서로 호출되면 Connection이 중첩될 수 있습니다.

**확인 방법:**
- 트랜잭션 로그에서 중첩 트랜잭션 확인
- `@Transactional(propagation = Propagation.REQUIRES_NEW)` 사용 여부 확인

## 즉시 조치 사항

### 1. Connection Validation 설정 확인

```properties
# Connection validation을 더 엄격하게 설정
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=5000
```

### 2. minimum-idle 설정

```properties
# 최소 Connection 수를 설정하여 Connection 재사용 촉진
spring.datasource.hikari.minimum-idle=2
```

### 3. "idle in transaction" 상태 확인

```sql
SELECT 
  pid,
  state,
  NOW() - query_start as idle_duration,
  query
FROM pg_stat_activity
WHERE application_name = 'workfolio-server-814c4072'
  AND state = 'idle in transaction';
```

### 4. HikariCP 로그 확인

```
# Connection 생성/반환 로그 확인
logging.level.com.zaxxer.hikari=DEBUG
```

## 근본 해결책

### 1. 외부 API 호출을 트랜잭션 밖으로 분리

가장 중요한 해결책입니다. 외부 API 호출이 트랜잭션 안에 있으면 Connection이 오래 점유됩니다.

### 2. Connection Pool 설정 최적화

```properties
# Connection 재사용을 촉진하는 설정
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.idle-timeout=180000
spring.datasource.hikari.max-lifetime=300000
```

### 3. Direct Connection 사용 검토

Supabase Transaction Pooler가 Connection 반환에 문제가 있다면, Direct Connection 사용을 검토해야 합니다.

## 모니터링

### 1. HikariCP Connection Pool 상태

`HikariConnectionPoolMonitor`가 30초마다 Connection Pool 상태를 로깅합니다.

### 2. PostgreSQL Connection 상태

```sql
-- Connection 상태 모니터링
SELECT 
  state,
  COUNT(*) as count,
  MAX(NOW() - query_start) as max_idle_duration
FROM pg_stat_activity
WHERE application_name = 'workfolio-server-814c4072'
GROUP BY state;
```

### 3. Connection 생성/반환 로그

HikariCP DEBUG 로그에서 Connection 생성/반환 패턴을 확인할 수 있습니다.

