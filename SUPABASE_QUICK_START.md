# 🚀 Supabase 빠른 시작 가이드

## ✅ 완료된 설정

### 1. **Database 연결 설정**
- ✅ `application.properties`: PostgreSQL JDBC URL 설정
- ✅ `build.gradle.kts`: PostgreSQL 드라이버 추가
- ✅ HikariCP 커넥션 풀 최적화 (Supabase 무료 플랜 대응)

### 2. **Supabase Storage 설정**
- ✅ `SupabaseConfig.kt`: Configuration Properties 클래스 생성
- ✅ `SupabaseStorageService.kt`: 파일 업로드/삭제 서비스 생성
- ✅ WebFlux 의존성 추가 (HTTP 클라이언트)

### 3. **환경 변수 설정**
- ✅ `docker-compose.env`: 환경 변수 템플릿
- ✅ `SUPABASE_SETUP.md`: 상세 설정 가이드

---

## 🔧 지금 해야 할 일

### Step 1: Supabase 비밀번호 및 API 키 확인

Supabase 대시보드(https://app.supabase.com)에 접속하여:

1. **Database Password**
   - Project Settings > Database > Connection string에서 `[YOUR-PASSWORD]` 확인

2. **API Keys**
   - Project Settings > API
   - `anon` `public` key 복사
   - `service_role` `secret` key 복사

### Step 2: 환경 변수 설정

**옵션 A: IntelliJ IDEA에서 실행 (개발 추천)**

1. `Run` > `Edit Configurations...`
2. Environment variables에 추가:
```
SUPABASE_DB_PASSWORD=실제_비밀번호;SUPABASE_ANON_KEY=실제_anon_키;SUPABASE_SERVICE_ROLE_KEY=실제_service_role_키
```

**옵션 B: 터미널에서 실행**

```bash
# 환경 변수 설정
export SUPABASE_DB_PASSWORD="실제_비밀번호"
export SUPABASE_ANON_KEY="실제_anon_키"
export SUPABASE_SERVICE_ROLE_KEY="실제_service_role_키"

# 애플리케이션 실행
./gradlew bootRun
```

**옵션 C: docker-compose.env 수정 (Docker 사용 시)**

```bash
# docker-compose.env 파일 열기
vi docker-compose.env

# 다음 값들을 실제 값으로 변경
SUPABASE_DB_PASSWORD=실제_비밀번호
SUPABASE_ANON_KEY=실제_anon_키
SUPABASE_SERVICE_ROLE_KEY=실제_service_role_키
```

### Step 3: Supabase Storage Bucket 생성

1. Supabase 대시보드 > Storage 메뉴
2. `Create bucket` 클릭
3. Bucket name: `workfolio-files`
4. Public 설정: 
   - ✅ Public (이미지 등 공개 파일)
   - ❌ Private (민감한 문서)

### Step 4: 의존성 다운로드 및 빌드

```bash
# Gradle 의존성 다운로드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

### Step 5: 연결 테스트

애플리케이션이 정상적으로 시작되면:

```
✅ Supabase PostgreSQL 연결 성공!
Started WorkfolioServerApplication in X.XXX seconds
```

---

## 📝 사용 예시

### 1. 파일 업로드 Controller 예시

```kotlin
@RestController
@RequestMapping("/api/files")
class FileController(
    private val storageService: SupabaseStorageService
) {
    
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): Map<String, String> {
        val fileUrl = storageService.uploadFile(file, "resumes")
        return mapOf("url" to fileUrl)
    }
    
    @DeleteMapping
    fun deleteFile(@RequestParam("url") fileUrl: String) {
        storageService.deleteFileByUrl(fileUrl)
    }
}
```

### 2. 파일 업로드 테스트 (cURL)

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/your/file.pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Postman 테스트

1. Method: `POST`
2. URL: `http://localhost:8080/api/files/upload`
3. Body: `form-data`
   - Key: `file` (type: File)
   - Value: 파일 선택

---

## 🎯 현재 설정 요약

```
┌─────────────────────┐
│   Spring Boot App   │
│   (localhost:8080)  │
└──────────┬──────────┘
           │
           ├─── Database ──────> Supabase PostgreSQL
           │                     (db.jxbmvvqjilxblzrojkek.supabase.co:5432)
           │
           ├─── Storage ───────> Supabase Storage
           │                     (workfolio-files bucket)
           │
           └─── Cache ─────────> Redis (로컬 Docker)
                                 (localhost:6379)
```

### 연결 정보

| 서비스 | 주소 | 포트 | 비고 |
|--------|------|------|------|
| PostgreSQL | db.jxbmvvqjilxblzrojkek.supabase.co | 5432 | Supabase 호스팅 |
| Storage API | jxbmvvqjilxblzrojkek.supabase.co | 443 | HTTPS |
| Redis | localhost (Docker) | 6379 | 로컬 |
| Spring Boot | localhost | 8080 | 개발 서버 |

---

## ⚡ 성능 최적화

### HikariCP 설정 (현재)
```properties
minimum-idle=2         # 최소 유휴 연결
maximum-pool-size=5    # 최대 연결 수 (Supabase 무료: 60개 제한)
connection-timeout=30s # 연결 타임아웃
max-lifetime=30m       # 연결 최대 수명
```

### Supabase 무료 플랜 제한
- 동시 연결: 최대 60개
- 데이터베이스: 500MB
- Storage: 1GB
- Bandwidth: 2GB/월

### Pro 플랜 ($25/월)
- 동시 연결: 최대 200개
- 데이터베이스: 8GB
- Storage: 100GB
- Bandwidth: 50GB/월

---

## 🔒 보안 주의사항

### ❌ 절대 하지 말 것
```kotlin
// 프론트엔드에 Service Role Key 노출 금지!
// Service Role Key는 모든 RLS를 우회합니다
val response = fetch("https://api.example.com", {
    headers: {
        "apikey": "서비스_롤_키"  // ❌ 위험!
    }
})
```

### ✅ 올바른 방법
```kotlin
// 백엔드에서만 Service Role Key 사용
@Service
class SupabaseStorageService(
    private val supabaseProperties: SupabaseProperties  // ✅ 안전
) {
    // Service Role Key는 서버에서만 사용
}

// 프론트엔드에서는 Anon Key 사용
const supabase = createClient(SUPABASE_URL, ANON_KEY)  // ✅ 안전
```

---

## 🐛 트러블슈팅

### 문제 1: Connection refused
```
org.postgresql.util.PSQLException: Connection refused
```

**해결:**
1. 환경 변수 확인: `SUPABASE_DB_PASSWORD`가 설정되었는지
2. Supabase 프로젝트가 Paused 상태가 아닌지 확인
3. 방화벽/VPN 확인

### 문제 2: Authentication failed
```
FATAL: password authentication failed for user "postgres"
```

**해결:**
```bash
# 비밀번호 다시 확인
# Supabase Dashboard > Settings > Database > Reset database password
```

### 문제 3: Too many connections
```
FATAL: remaining connection slots are reserved
```

**해결:**
```properties
# application.properties에서 풀 크기 줄이기
spring.datasource.hikari.maximum-pool-size=3
```

### 문제 4: IPv6 network
```
Connection error: Network is unreachable
```

**해결:**
```properties
# Session Pooler 사용 (포트 6543)
spring.datasource.hikari.jdbc-url=jdbc:postgresql://db.jxbmvvqjilxblzrojkek.supabase.co:6543/postgres
```

---

## 📚 다음 단계

- [ ] Supabase Row Level Security (RLS) 설정
- [ ] 파일 업로드 크기 제한 설정
- [ ] 이미지 리사이징 (Supabase Image Transformation)
- [ ] CDN 설정 (Supabase CDN 또는 Cloudflare)
- [ ] 백업 전략 수립
- [ ] 모니터링 설정 (Supabase Dashboard)

상세 가이드는 `SUPABASE_SETUP.md`를 참고하세요!

---

## 💡 유용한 명령어

```bash
# PostgreSQL 직접 접속
psql "postgresql://postgres:[비밀번호]@db.jxbmvvqjilxblzrojkek.supabase.co:5432/postgres"

# 테이블 목록 확인
\dt

# 특정 테이블 구조 확인
\d 테이블명

# 종료
\q
```

---

## 🎉 완료!

모든 설정이 완료되었습니다! 이제 Supabase를 사용할 준비가 되었습니다.

문제가 발생하면 `SUPABASE_SETUP.md`의 상세 가이드를 확인하세요.

