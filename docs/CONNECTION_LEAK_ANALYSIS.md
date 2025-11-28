# Connection Leak 분석 및 해결 가이드

## 문제 상황

Connection이 반환되지 않고 계속 누적되는 문제가 발생했습니다.

### 증상
- 07:41에 10개 Connection 생성
- 07:50에 6개 추가 생성 (총 16개)
- 07:51에 1개 추가 생성 (총 17개)
- 기존 Connection을 재사용하지 않고 계속 새로 생성

### 원인 분석

#### 1. Transaction Pooler 모드 확인
현재 포트 5432를 사용 중이므로 Transaction Pooler 모드입니다.
- ✅ Transaction Pooler는 Connection이 트랜잭션 종료 시 반환되어야 함
- ❌ 하지만 Connection이 반환되지 않고 있음

#### 2. 가능한 원인
1. **트랜잭션이 제대로 종료되지 않음**
   - 예외 발생 시 롤백되지 않음
   - 트랜잭션 타임아웃 없음

2. **외부 API 호출로 인한 긴 트랜잭션**
   - `createAttachment`에서 Supabase Storage 업로드
   - 트랜잭션 안에서 외부 API 호출

3. **Session Pooler 모드로 인한 문제**
   - 포트 5432는 Transaction Pooler이지만, Supabase 설정에 따라 다를 수 있음
   - Shared Pooler가 Session 모드로 동작할 수 있음

## 해결 방법

### 1. 트랜잭션 타임아웃 설정

모든 `@Transactional` 메서드에 타임아웃 설정:

```kotlin
@Transactional(timeout = 30)  // 30초 타임아웃
fun createAttachment(dto: AttachmentCreateDto): Attachment {
    // ...
}
```

### 2. 외부 API 호출을 트랜잭션 밖으로 분리

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

### 3. Connection Pool 설정 최적화

```properties
# 더 공격적인 Connection 정리
spring.datasource.hikari.idle-timeout=30000  # 30초
spring.datasource.hikari.max-lifetime=120000  # 2분
```

### 4. Connection Leak 모니터링

애플리케이션 로그에서 다음을 확인:
```
Connection leak detection triggered
```

## 확인 쿼리

### Connection 생성 패턴 확인
```sql
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

### Connection Leak 확인
```sql
SELECT 
  pid,
  application_name,
  state,
  backend_start,
  query_start,
  NOW() - backend_start as connection_duration,
  NOW() - query_start as idle_duration
FROM pg_stat_activity
WHERE datname = 'postgres'
  AND application_name = 'PostgreSQL JDBC Driver'
  AND state = 'idle'
  AND NOW() - query_start > INTERVAL '5 minutes'
ORDER BY query_start;
```

## 예방 조치

1. 모든 `@Transactional` 메서드에 타임아웃 설정
2. 외부 API 호출을 트랜잭션 밖으로 분리
3. Connection Pool 모니터링 활성화
4. 정기적인 Connection Leak 확인

