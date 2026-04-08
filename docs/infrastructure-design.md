# Workfolio 인프라 설계 문서

## 1. 개요

Workfolio 서비스의 프로덕션 인프라 구성을 정의한다.
프론트엔드는 Vercel, 백엔드는 Render, 데이터베이스 및 스토리지는 Supabase를 사용한다.

### 서비스 요약

| 구성 요소 | 플랫폼 | 용도 |
|-----------|--------|------|
| Frontend (Next.js) | Vercel | SSR/SSG 웹 애플리케이션 호스팅 |
| Backend (Spring Boot) | Render | REST API 서버 |
| Redis | Render (Key-Value Store) | JWT 블랙리스트, 세션 캐싱 |
| PostgreSQL | Supabase | 메인 데이터베이스 |
| Object Storage | Supabase Storage | 파일 첨부 (S3 호환) |

### 도메인 구성

| 도메인 | 대상 | 용도 |
|--------|------|------|
| `workfolio.spectrify.kr` | Vercel | 프론트엔드 |
| `api.workfolio.kr` | Render | 백엔드 API |

---

## 2. 전체 아키텍처

```
                         ┌──────────────────────────────┐
                         │        DNS (workfolio.kr)     │
                         └──────┬───────────┬───────────┘
                                │           │
                    workfolio.spectrify.kr    api.workfolio.kr
                                │           │
                    ┌───────────▼──┐  ┌─────▼───────────┐
                    │              │  │                  │
                    │   Vercel     │  │   Render         │
                    │   (Next.js)  │──▶  (Spring Boot)   │
                    │              │  │   Port 9000      │
                    └──────────────┘  └──┬──────────┬───┘
                                        │          │
                              ┌─────────▼──┐  ┌───▼──────────────┐
                              │            │  │                  │
                              │  Render    │  │   Supabase       │
                              │  Redis     │  │  ┌────────────┐  │
                              │  (캐시)    │  │  │ PostgreSQL │  │
                              │            │  │  │ (Pooler)   │  │
                              └────────────┘  │  ├────────────┤  │
                                              │  │  Storage   │  │
                                              │  │  (S3 호환) │  │
                                              └──┴────────────┴──┘
```

---

## 3. Vercel (프론트엔드)

### 3.1 프로젝트 설정

| 항목 | 값 |
|------|-----|
| Framework | Next.js (Turbopack) |
| Build Command | `pnpm turbo build --filter=app` |
| Output Directory | `apps/app/.next` |
| Node.js Version | 20.x |
| Region | `icn1` (서울) |

### 3.2 환경 변수

| 변수 | 설명 |
|------|------|
| `NEXT_PUBLIC_API_URL` | `https://api.workfolio.kr` |
| `NEXT_PUBLIC_SUPABASE_URL` | Supabase 프로젝트 URL |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | Supabase 공개 키 |

### 3.3 배포 전략

- **Production**: `main` 브랜치 push 시 자동 배포
- **Preview**: PR 생성 시 Preview URL 자동 생성
- **Rollback**: Vercel 대시보드에서 이전 배포로 즉시 롤백 가능

### 3.4 커스텀 도메인

- `workfolio.kr` → Vercel에 A/CNAME 레코드 연결
- `workfolio.spectrify.kr` → CNAME → `cname.vercel-dns.com`
- SSL 인증서 자동 발급 (Let's Encrypt)

---

## 4. Render (백엔드)

### 4.1 Web Service 설정

| 항목 | 값 |
|------|-----|
| Name | `workfolio-server` |
| Environment | Docker |
| Region | Singapore |
| Plan | Standard ($7/month) |
| Dockerfile | `Dockerfile.render` |
| Health Check | `/actuator/health` |
| Port | 9000 |

### 4.2 Docker 빌드 (Dockerfile.render)

```dockerfile
# 빌드 스테이지
FROM amazoncorretto:21-alpine-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test -x ktlintCheck -x detekt

# 런타임 스테이지
FROM amazoncorretto:21-alpine-jdk
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
USER appuser
EXPOSE 9000
HEALTHCHECK CMD wget -qO- http://localhost:9000/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.3 환경 변수

| 변수 | 값 / 소스 |
|------|-----------|
| `PORT` | `9000` |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_PASSWORD` | Render Dashboard에서 수동 입력 |
| `SUPABASE_ACCESS_KEY` | Render Dashboard에서 수동 입력 |
| `SUPABASE_SECRET_KEY` | Render Dashboard에서 수동 입력 |
| `SPRING_DATA_REDIS_URL` | Render Redis 서비스에서 자동 주입 |

### 4.4 Redis (Key-Value Store)

| 항목 | 값 |
|------|-----|
| Name | `workfolio-redis` |
| Region | Singapore |
| Plan | Free (25MB) |
| Eviction Policy | `allkeys-lru` |

**용도:**
- JWT 토큰 블랙리스트 (로그아웃 시)
- 임시 데이터 캐싱

### 4.5 배포 전략

- `main` 브랜치 push 시 자동 배포 (render.yaml에 의해 구성)
- Zero-downtime deploy (새 컨테이너가 health check 통과 후 트래픽 전환)
- Health check 실패 시 이전 버전 유지

### 4.6 커스텀 도메인

- `api.workfolio.kr` → Render 서비스에 CNAME 연결
- SSL 인증서 자동 관리

### 4.7 스케일링 고려사항

| 단계 | 조건 | 대응 |
|------|------|------|
| 현재 | 초기 서비스 | Standard 플랜 단일 인스턴스 |
| 성장기 | 응답 지연 증가 시 | Standard Plus로 업그레이드 |
| 확장기 | 동시 접속 증가 시 | Pro 플랜 + 인스턴스 수 증가 |

---

## 5. Supabase (데이터베이스 & 스토리지)

### 5.1 PostgreSQL

| 항목 | 값 |
|------|-----|
| 버전 | PostgreSQL 16 |
| Region | ap-northeast-2 (서울) |
| Connection Pooler | Supavisor (Transaction Mode) |

**연결 정보:**
```
Host: aws-1-ap-northeast-2.pooler.supabase.com
Port: 5432
Database: postgres
User: postgres.{project-ref}
```

**Connection Pool 설정 (HikariCP):**
```properties
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.idle-timeout=0
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.max-lifetime=86400000
```

**JDBC 연결 최적화 (Supabase Pooler 호환):**
```
?prepareThreshold=0&preparedStatementCacheQueries=0
```
> Supabase Pooler(Transaction Mode)에서는 prepared statement가 세션 간에 공유되지 않으므로 비활성화한다.

### 5.2 스키마 관리 (Liquibase)

- 마이그레이션 파일: `src/main/resources/db/primary/`
- 서버 시작 시 자동 마이그레이션 실행
- 변경 이력 관리: `db.changelog-master.yaml`

### 5.3 Supabase Storage (파일 스토리지)

| 항목 | 값 |
|------|-----|
| 프로토콜 | S3 호환 API |
| SDK | AWS SDK 2.x |
| 용도 | 첨부파일 업로드/다운로드 |

**Spring Boot 연동:**
```kotlin
// S3 호환 클라이언트 설정
S3Client.builder()
    .endpointOverride(URI("https://{project-ref}.supabase.co/storage/v1/s3"))
    .region(Region.AP_NORTHEAST_2)
    .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey)
    ))
    .build()
```

### 5.4 백업 전략

| 항목 | 설명 |
|------|------|
| 자동 백업 | Supabase에서 일 단위 자동 백업 (Pro 플랜) |
| Point-in-Time Recovery | Pro 플랜에서 지원 (최대 7일) |
| 수동 백업 | `pg_dump`로 주기적 백업 권장 |

---

## 6. 네트워크 & 보안

### 6.1 통신 흐름

```
사용자 → Vercel (HTTPS) → Render API (HTTPS) → Supabase DB (SSL)
                                              → Render Redis (내부 통신)
                                              → Supabase Storage (HTTPS)
```

### 6.2 CORS 설정

```kotlin
// SecurityConfig.kt
allowedOrigins = ["https://workfolio.spectrify.kr"]
allowedMethods = ["GET", "POST", "PUT", "DELETE", "PATCH"]
allowCredentials = true
```

### 6.3 인증 흐름

```
1. 사용자 → Vercel 프론트엔드 → Kakao OAuth2 로그인 요청
2. Kakao → 인가 코드 → Render 백엔드 콜백
3. 백엔드 → Kakao Token API → 사용자 정보 조회
4. 백엔드 → JWT Access Token (3시간) + Refresh Token (7일) 발급
5. 이후 요청: Authorization: Bearer {accessToken}
6. 로그아웃 시: Redis에 토큰 블랙리스트 등록
```

### 6.4 환경 변수 관리

| 분류 | 관리 방식 |
|------|-----------|
| 프론트엔드 (공개) | Vercel 환경 변수 (`NEXT_PUBLIC_*`) |
| 프론트엔드 (비공개) | Vercel 환경 변수 (서버 전용) |
| 백엔드 | Render Dashboard (sync: false 로 보호) |
| DB 비밀번호 | Render Dashboard 수동 입력 |
| Supabase 키 | Render Dashboard 수동 입력 |

---

## 7. CI/CD 파이프라인

### 7.1 프론트엔드 (Vercel)

```
main push/PR merge
    → Vercel 자동 빌드 트리거
    → pnpm install
    → pnpm turbo build --filter=app
    → Preview/Production 배포
    → 배포 URL 자동 생성
```

### 7.2 백엔드 (Render)

```
main push/PR merge
    → Render 자동 빌드 트리거
    → Docker 이미지 빌드 (Dockerfile.render)
    → Gradle bootJar (테스트 스킵)
    → 새 컨테이너 시작
    → Health Check (/actuator/health) 통과 확인
    → 트래픽 전환 (Zero-downtime)
```

### 7.3 데이터베이스 마이그레이션

```
백엔드 서버 시작 시
    → Liquibase 자동 실행
    → db.changelog-master.yaml 기반 마이그레이션
    → DATABASECHANGELOG 테이블에 이력 기록
```

---

## 8. 모니터링

### 8.1 헬스 체크

| 대상 | 엔드포인트 | 주기 |
|------|-----------|------|
| Backend | `/actuator/health` | Render 자동 (10초) |
| Database | HikariCP 커넥션 체크 | Actuator health indicator |
| Redis | Redis ping | Actuator health indicator |

### 8.2 로깅

| 환경 | 방식 |
|------|------|
| 개발 | 로컬 콘솔 출력 |
| 프로덕션 | Render 대시보드 로그 뷰어 |

### 8.3 향후 모니터링 확장 (선택)

- Prometheus + Grafana: Spring Boot Actuator 메트릭 수집 (docker-compose에 설정 준비됨)
- Sentry: 프론트엔드/백엔드 에러 트래킹
- Uptime Robot: 외부 가용성 모니터링

---

## 9. 비용 추정

| 서비스 | 플랜 | 월 비용 (USD) |
|--------|------|---------------|
| Vercel | Hobby (개인) / Pro (팀) | $0 ~ $20 |
| Render Web Service | Standard | $7 |
| Render Redis | Free | $0 |
| Supabase | Free / Pro | $0 ~ $25 |
| 도메인 (workfolio.kr) | 연간 | ~$10/년 |
| **합계** | | **$7 ~ $52/월** |

> Free tier로 시작하면 월 $7 수준에서 운영 가능. 트래픽 증가 시 Supabase Pro($25)와 Vercel Pro($20)로 업그레이드.

---

## 10. 로컬 개발 환경

프로덕션과 동일한 스택을 로컬에서 Docker Compose로 구성:

```bash
# 로컬 환경 실행
docker compose up -d

# 서비스 구성
# - workfolio-server: localhost:9000
# - PostgreSQL:       localhost:5432
# - Redis:            localhost:6379
```

| 프로덕션 | 로컬 |
|----------|------|
| Supabase PostgreSQL | Docker PostgreSQL 16 |
| Render Redis | Docker Redis 8.2.0 |
| Render Web Service | `./gradlew bootRun` 또는 Docker |
| Vercel | `pnpm dev` (localhost:4000) |

---

## 11. 리전 및 지연시간

| 서비스 | 리전 | 비고 |
|--------|------|------|
| Vercel | Seoul (icn1) | Edge Network로 글로벌 CDN |
| Render | Singapore | 한국에서 가장 가까운 리전 |
| Supabase | ap-northeast-2 (서울) | DB 직접 접근 |

> Render(싱가포르) ↔ Supabase(서울) 간 지연시간은 약 50~80ms 수준.
> 향후 Render에 서울 리전이 추가되면 마이그레이션 고려.

---

## 부록: render.yaml 설정 파일

현재 프로젝트에 적용된 `render.yaml`:

```yaml
services:
  - type: web
    name: workfolio-server
    env: docker
    region: singapore
    plan: standard
    dockerfilePath: ./Dockerfile.render
    healthCheckPath: /actuator/health
    envVars:
      - key: PORT
        value: "9000"
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: DB_PASSWORD
        sync: false
      - key: SUPABASE_ACCESS_KEY
        sync: false
      - key: SUPABASE_SECRET_KEY
        sync: false
      - key: SPRING_DATA_REDIS_URL
        fromService:
          name: workfolio-redis
          type: redis
          property: connectionString

  - type: redis
    name: workfolio-redis
    region: singapore
    plan: free
    maxmemoryPolicy: allkeys-lru
```
