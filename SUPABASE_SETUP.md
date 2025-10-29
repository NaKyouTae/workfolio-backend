# Supabase 연결 설정 가이드

## 📋 개요
이 문서는 Workfolio 백엔드를 Supabase에 연결하는 방법을 설명합니다.

## 🔑 필요한 정보

Supabase 대시보드(https://app.supabase.com)에서 다음 정보를 확인하세요:

### 1. Database 정보
- **Host**: `db.jxbmvvqjilxblzrojkek.supabase.co`
- **Port**: `5432`
- **Database**: `postgres`
- **User**: `postgres`
- **Password**: Supabase 프로젝트 설정에서 확인

### 2. API 정보
- **Project URL**: `https://jxbmvvqjilxblzrojkek.supabase.co`
- **Anon Key**: Project Settings > API > `anon` `public` key
- **Service Role Key**: Project Settings > API > `service_role` `secret` key

## ⚙️ 환경 변수 설정

### 방법 1: IntelliJ IDEA 실행 구성 (개발용)

1. IntelliJ에서 `Run` > `Edit Configurations...`
2. Spring Boot 실행 구성 선택
3. `Environment variables` 섹션에 추가:

```
SUPABASE_DB_PASSWORD=your_actual_password;
SUPABASE_ANON_KEY=your_anon_key;
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
```

### 방법 2: 시스템 환경 변수 (macOS/Linux)

```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
export SUPABASE_DB_PASSWORD="your_actual_password"
export SUPABASE_ANON_KEY="your_anon_key"
export SUPABASE_SERVICE_ROLE_KEY="your_service_role_key"
export SUPABASE_URL="https://jxbmvvqjilxblzrojkek.supabase.co"
export SUPABASE_STORAGE_BUCKET="workfolio-files"

# 적용
source ~/.zshrc
```

### 방법 3: Docker Compose 환경 변수

`docker-compose.env` 파일에 추가:

```properties
# Supabase Configuration
SUPABASE_DB_PASSWORD=your_actual_password
SUPABASE_URL=https://jxbmvvqjilxblzrojkek.supabase.co
SUPABASE_ANON_KEY=your_anon_key
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
SUPABASE_STORAGE_BUCKET=workfolio-files
```

### 방법 4: application-prod.properties 생성 (프로덕션용)

```properties
# src/main/resources/application-prod.properties
spring.datasource.hikari.jdbc-url=jdbc:postgresql://db.jxbmvvqjilxblzrojkek.supabase.co:5432/postgres?reWriteBatchedInserts=true&stringtype=unspecified
spring.datasource.username=postgres
spring.datasource.password=${SUPABASE_DB_PASSWORD}

supabase.url=${SUPABASE_URL}
supabase.anon-key=${SUPABASE_ANON_KEY}
supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}
```

## 🗄️ Supabase Storage 설정

### 1. Bucket 생성

Supabase 대시보드에서:
1. `Storage` 메뉴 선택
2. `Create bucket` 클릭
3. Bucket name: `workfolio-files`
4. Public 여부 선택 (추천: Private)

### 2. Bucket Policies 설정 (선택사항)

```sql
-- 인증된 사용자만 업로드 가능
CREATE POLICY "Authenticated users can upload files"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'workfolio-files');

-- 파일 소유자만 조회 가능
CREATE POLICY "Users can view their own files"
ON storage.objects FOR SELECT
TO authenticated
USING (bucket_id = 'workfolio-files' AND auth.uid() = owner);
```

## 🔌 연결 테스트

### 1. 데이터베이스 연결 확인

```bash
# psql 사용
psql "postgresql://postgres:[YOUR_PASSWORD]@db.jxbmvvqjilxblzrojkek.supabase.co:5432/postgres"

# 또는 애플리케이션 실행
./gradlew bootRun
```

### 2. Supabase REST API 테스트

```bash
curl -X GET \
  'https://jxbmvvqjilxblzrojkek.supabase.co/rest/v1/' \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Authorization: Bearer YOUR_ANON_KEY"
```

## 📦 Gradle 의존성 추가 (Supabase Storage 사용 시)

```kotlin
// build.gradle.kts에 추가
dependencies {
    // Supabase Storage용 (HTTP 클라이언트)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // 또는 OkHttp 사용
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

## 🔧 Supabase Configuration 클래스 생성

```kotlin
// src/main/kotlin/com/spectrum/workfolio/config/SupabaseConfig.kt
package com.spectrum.workfolio.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "supabase")
data class SupabaseProperties(
    var url: String = "",
    var anonKey: String = "",
    var serviceRoleKey: String = "",
    var storage: StorageProperties = StorageProperties()
) {
    data class StorageProperties(
        var bucket: String = "workfolio-files"
    )
}
```

## 🚀 Supabase Storage Service 예시

```kotlin
// src/main/kotlin/com/spectrum/workfolio/services/SupabaseStorageService.kt
package com.spectrum.workfolio.services

import com.spectrum.workfolio.config.SupabaseProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Service
class SupabaseStorageService(
    private val supabaseProperties: SupabaseProperties,
    private val webClientBuilder: WebClient.Builder
) {
    private val webClient: WebClient = webClientBuilder
        .baseUrl("${supabaseProperties.url}/storage/v1")
        .defaultHeader("apikey", supabaseProperties.serviceRoleKey)
        .defaultHeader("Authorization", "Bearer ${supabaseProperties.serviceRoleKey}")
        .build()

    fun uploadFile(file: MultipartFile, folder: String = ""): String {
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        val path = if (folder.isNotEmpty()) "$folder/$fileName" else fileName

        webClient.post()
            .uri("/object/${supabaseProperties.storage.bucket}/$path")
            .bodyValue(file.bytes)
            .retrieve()
            .bodyToMono(String::class.java)
            .block()

        return "${supabaseProperties.url}/storage/v1/object/public/${supabaseProperties.storage.bucket}/$path"
    }

    fun deleteFile(filePath: String) {
        webClient.delete()
            .uri("/object/${supabaseProperties.storage.bucket}/$filePath")
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}
```

## 📝 주의사항

### 1. Connection Pooling
Supabase는 무료 플랜에서 동시 연결 수 제한이 있습니다:
- **Free Plan**: 최대 60개 동시 연결
- **Pro Plan**: 최대 200개 동시 연결

현재 설정에서는 `maximum-pool-size=5`로 충분합니다.

### 2. IPv6 이슈
Supabase는 IPv4만 지원합니다. IPv6 네트워크에서는 Session Pooler를 사용하거나 IPv4 add-on을 구매해야 합니다.

**Session Pooler 사용 (권장):**
```properties
# Session Pooler는 6543 포트 사용
spring.datasource.hikari.jdbc-url=jdbc:postgresql://db.jxbmvvqjilxblzrojkek.supabase.co:6543/postgres?reWriteBatchedInserts=true
```

### 3. 보안
- **절대로** Service Role Key를 클라이언트(프론트엔드)에 노출하지 마세요
- 프론트엔드에서는 Anon Key만 사용
- 백엔드에서는 Service Role Key 사용 (모든 권한)

### 4. Row Level Security (RLS)
Supabase는 기본적으로 RLS를 권장합니다. 테이블 생성 시:

```sql
-- RLS 활성화
ALTER TABLE your_table ENABLE ROW LEVEL SECURITY;

-- Policy 생성
CREATE POLICY "Users can only access their own data"
ON your_table
FOR ALL
USING (auth.uid() = user_id);
```

## 🧪 테스트

### 연결 테스트 코드

```kotlin
@SpringBootTest
class SupabaseConnectionTest {
    
    @Autowired
    lateinit var dataSource: DataSource
    
    @Test
    fun `Supabase 데이터베이스 연결 테스트`() {
        dataSource.connection.use { connection ->
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT version()")
            if (resultSet.next()) {
                println("PostgreSQL Version: ${resultSet.getString(1)}")
            }
        }
    }
}
```

## 📚 참고 자료

- [Supabase 공식 문서](https://supabase.com/docs)
- [Supabase Database 가이드](https://supabase.com/docs/guides/database)
- [Supabase Storage 가이드](https://supabase.com/docs/guides/storage)
- [PostgreSQL JDBC 드라이버](https://jdbc.postgresql.org/documentation/)

---

문제가 발생하면 다음을 확인하세요:
1. Supabase 프로젝트가 활성 상태인지
2. 환경 변수가 올바르게 설정되었는지
3. 네트워크/방화벽 설정
4. Supabase 대시보드에서 Database Health 확인

