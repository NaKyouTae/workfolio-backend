# Supabase Storage 설정 가이드

## Supabase Storage 정보

### 기본 정보
- **Supabase Project URL**: `https://jxbmvvqjilxblzrojkek.supabase.co`
- **Storage S3 Endpoint**: `https://jxbmvvqjilxblzrojkek.storage.supabase.co/storage/v1/s3`
- **Region**: `ap-northeast-2` (Seoul, South Korea)
- **Bucket**: `workfolio`
- **Base Path**: `resumes/attachments`

### 파일 저장 구조

```
workfolio/                              # Bucket
  └── resumes/                          # Base Path
      └── attachments/                  # Base Path
          ├── {userId1}/                # 사용자별 폴더 (선택)
          │   ├── 20251029140530_a1b2c3d4.png
          │   └── 20251029141020_e5f6g7h8.pdf
          ├── {userId2}/
          │   └── 20251029142530_i9j0k1l2.jpg
          └── 20251029143045_m3n4o5p6.docx  # 폴더 없이 직접 저장도 가능
```

### Public URL 형식

```
https://jxbmvvqjilxblzrojkek.supabase.co/storage/v1/object/public/workfolio/resumes/attachments/{fileName}
```

또는 사용자별 폴더가 있는 경우:

```
https://jxbmvvqjilxblzrojkek.supabase.co/storage/v1/object/public/workfolio/resumes/attachments/{userId}/{fileName}
```

---

## 환경 변수 설정

### 1. `.env` 파일

프로젝트 루트에 `.env` 파일을 생성하거나 수정:

```bash
# Supabase Database
SUPABASE_DB_PASSWORD=your_actual_password_here

# Supabase Storage Configuration
SUPABASE_URL=https://jxbmvvqjilxblzrojkek.supabase.co
SUPABASE_STORAGE_URL=https://jxbmvvqjilxblzrojkek.storage.supabase.co/storage/v1/s3
SUPABASE_REGION=ap-northeast-2

# Supabase API Keys (Supabase Dashboard → Settings → API에서 확인)
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Storage Bucket Configuration
SUPABASE_STORAGE_BUCKET=workfolio
SUPABASE_STORAGE_BASE_PATH=resumes/attachments
```

### 2. IntelliJ IDEA 설정

Run → Edit Configurations → Environment variables:

```
SUPABASE_URL=https://jxbmvvqjilxblzrojkek.supabase.co;SUPABASE_STORAGE_URL=https://jxbmvvqjilxblzrojkek.storage.supabase.co/storage/v1/s3;SUPABASE_REGION=ap-northeast-2;SUPABASE_ANON_KEY=your_anon_key;SUPABASE_SERVICE_ROLE_KEY=your_service_key;SUPABASE_STORAGE_BUCKET=workfolio;SUPABASE_STORAGE_BASE_PATH=resumes/attachments;SUPABASE_DB_PASSWORD=your_password
```

---

## Supabase Dashboard 설정

### 1. Storage Bucket 생성

1. Supabase Dashboard 접속: https://supabase.com/dashboard
2. 프로젝트 선택
3. Storage → Buckets → Create Bucket
4. Bucket 이름: `workfolio`
5. Public bucket 설정 (체크)
6. Create

### 2. Storage Policy 설정 (보안)

기본적으로 Public Bucket은 누구나 읽을 수 있지만, 쓰기는 인증된 사용자만 가능하도록 설정:

```sql
-- 읽기: 모든 사용자 허용
CREATE POLICY "Public Access"
ON storage.objects FOR SELECT
USING ( bucket_id = 'workfolio' );

-- 쓰기: 인증된 사용자만 허용
CREATE POLICY "Authenticated users can upload"
ON storage.objects FOR INSERT
WITH CHECK (
  bucket_id = 'workfolio' 
  AND auth.role() = 'authenticated'
);

-- 삭제: 본인이 업로드한 파일만 삭제 가능
CREATE POLICY "Users can delete own files"
ON storage.objects FOR DELETE
USING (
  bucket_id = 'workfolio' 
  AND auth.uid()::text = (storage.foldername(name))[1]
);
```

### 3. API Keys 확인

1. Settings → API
2. **Project URL** 복사 → `SUPABASE_URL`
3. **anon public** key 복사 → `SUPABASE_ANON_KEY`
4. **service_role** key 복사 → `SUPABASE_SERVICE_ROLE_KEY`

⚠️ **주의**: `service_role` 키는 절대 프론트엔드에 노출하지 말고, 백엔드에서만 사용해야 합니다!

---

## 애플리케이션 설정 확인

### SupabaseConfig.kt

```kotlin
@Configuration
@ConfigurationProperties(prefix = "supabase")
data class SupabaseConfig(
    var url: String = "",
    var storageUrl: String = "",
    var region: String = "ap-northeast-2",
    var anonKey: String = "",
    var serviceRoleKey: String = "",
    var storageBucket: String = "workfolio",
    var storageBasePath: String = "resumes/attachments"
)
```

### application.properties

```properties
# Supabase Configuration
supabase.url=${SUPABASE_URL:https://jxbmvvqjilxblzrojkek.supabase.co}
supabase.storage-url=${SUPABASE_STORAGE_URL:https://jxbmvvqjilxblzrojkek.storage.supabase.co/storage/v1/s3}
supabase.region=${SUPABASE_REGION:ap-northeast-2}
supabase.anon-key=${SUPABASE_ANON_KEY}
supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}
supabase.storage-bucket=${SUPABASE_STORAGE_BUCKET:workfolio}
supabase.storage-base-path=${SUPABASE_STORAGE_BASE_PATH:resumes/attachments}
```

---

## 파일 업로드 API 사용법

### 1. 기본 업로드 (resumes/attachments/ 경로에 직접 저장)

**요청**:
```http
POST /api/attachments/upload
Content-Type: application/x-protobuf

[Protobuf Binary Data]
```

**결과 경로**:
```
workfolio/resumes/attachments/20251029140530_a1b2c3d4.png
```

### 2. 사용자별 폴더로 업로드

**요청**:
```http
POST /api/attachments/upload?folder={userId}
Content-Type: application/x-protobuf

[Protobuf Binary Data]
```

**결과 경로**:
```
workfolio/resumes/attachments/{userId}/20251029140530_a1b2c3d4.png
```

### 3. 프론트엔드 예시 (TypeScript)

```typescript
// 사용자별 폴더에 업로드
const uploadToUserFolder = async (file: File, userId: string) => {
  const base64Data = await fileToBase64(file);
  
  const uploadRequest = attachment.AttachmentUploadRequest.create({
    fileName: file.name,
    contentType: file.type,
    fileDataBase64: base64Data
  });
  
  const buffer = attachment.AttachmentUploadRequest.encode(uploadRequest).finish();
  
  const response = await fetch(
    `http://localhost:8080/api/attachments/upload?folder=${userId}`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-protobuf' },
      body: buffer
    }
  );
  
  return await response.json();
};
```

---

## 파일 접근 URL

### Public Access (브라우저에서 직접 접근)

```
https://jxbmvvqjilxblzrojkek.supabase.co/storage/v1/object/public/workfolio/resumes/attachments/20251029140530_a1b2c3d4.png
```

### Authenticated Access (인증 필요)

Private bucket인 경우, Signed URL을 생성해야 합니다:

```kotlin
// SupabaseStorageService에 추가 메서드
fun createSignedUrl(filePath: String, expiresIn: Int = 3600): String {
    val response = webClient.post()
        .uri("/object/sign/${supabaseConfig.storageBucket}/$filePath")
        .bodyValue(mapOf("expiresIn" to expiresIn))
        .retrieve()
        .bodyToMono<Map<String, String>>()
        .block()
    
    return response?.get("signedURL") ?: throw WorkfolioException("Signed URL 생성 실패")
}
```

---

## 로그 확인

애플리케이션 시작 시 다음과 같은 로그가 출력되어야 합니다:

```
INFO  SupabaseStorageService - Supabase Storage initialized:
INFO  SupabaseStorageService -   - Base URL: https://jxbmvvqjilxblzrojkek.supabase.co
INFO  SupabaseStorageService -   - Storage URL: https://jxbmvvqjilxblzrojkek.storage.supabase.co/storage/v1/s3
INFO  SupabaseStorageService -   - Region: ap-northeast-2
INFO  SupabaseStorageService -   - Bucket: workfolio
INFO  SupabaseStorageService -   - Base Path: resumes/attachments
```

---

## 트러블슈팅

### 1. 업로드 실패: "Bucket not found"

**원인**: Supabase Dashboard에서 bucket이 생성되지 않음

**해결**:
1. Supabase Dashboard → Storage → Buckets
2. `workfolio` bucket이 있는지 확인
3. 없으면 Create Bucket으로 생성

### 2. 업로드 실패: "Unauthorized"

**원인**: API 키가 잘못되었거나 환경 변수가 설정되지 않음

**해결**:
1. `.env` 파일 또는 IntelliJ 환경 변수 확인
2. Supabase Dashboard → Settings → API에서 키 재확인
3. `SUPABASE_SERVICE_ROLE_KEY`가 올바르게 설정되었는지 확인

### 3. 파일 접근 실패: "Object not found"

**원인**: 파일 경로가 잘못되었거나 Public Access가 설정되지 않음

**해결**:
1. Supabase Dashboard → Storage → `workfolio` bucket
2. Configuration → Public 설정 확인
3. URL이 정확한지 확인

### 4. CORS 에러

**원인**: 프론트엔드와 백엔드 간 CORS 설정 누락

**해결**:
```kotlin
// WebMvcConfig.kt
@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
```

---

## 비용 안내

### Supabase Storage 가격 (2025년 기준)

- **Free Tier**: 1GB 스토리지 + 2GB 전송량/월
- **Pro Plan ($25/월)**: 100GB 스토리지 + 200GB 전송량/월
- **초과 비용**: 
  - 스토리지: $0.021/GB/월
  - 전송량: $0.09/GB

### 최적화 팁

1. **이미지 압축**: 프론트엔드에서 업로드 전에 이미지 리사이징/압축
2. **CDN 사용**: Supabase Storage는 기본적으로 CDN 제공
3. **파일 크기 제한**: 백엔드에서 최대 파일 크기 제한 (현재 10MB)
4. **불필요한 파일 정리**: 정기적으로 사용하지 않는 파일 삭제

---

## 참고 자료

- [Supabase Storage Documentation](https://supabase.com/docs/guides/storage)
- [Supabase Storage API Reference](https://supabase.com/docs/reference/javascript/storage)
- [Protobuf Guide](https://protobuf.dev/)

