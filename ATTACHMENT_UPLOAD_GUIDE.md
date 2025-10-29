# Protobuf 기반 파일 업로드 가이드

## 개요
이 가이드는 React 프론트엔드에서 Base64로 인코딩된 파일 데이터를 Protobuf 형식으로 Spring Boot 백엔드에 전송하는 방법을 설명합니다.

## 백엔드 구조

### 1. Protobuf 메시지 정의 (`attachment.proto`)

```protobuf
message AttachmentUploadRequest {
  string file_name = 1;
  string content_type = 2;
  string file_data_base64 = 3;
  optional string description = 4;
  repeated string tags = 5;
}

message AttachmentUploadResponse {
  string id = 1;
  string file_name = 2;
  string file_url = 3;
  int64 file_size = 4;
  string content_type = 5;
  string created_at = 6;
}
```

### 2. API 엔드포인트
- **URL**: `POST /api/attachments/upload`
- **Content-Type**: `application/x-protobuf` 또는 `application/octet-stream`
- **Request Body**: Base64로 인코딩된 파일 데이터를 포함한 Protobuf 바이트

### 3. 처리 흐름
1. 프론트엔드에서 파일을 Base64로 인코딩
2. Protobuf 메시지에 Base64 문자열 포함
3. Protobuf 메시지를 바이너리로 직렬화
4. HTTP POST 요청으로 전송
5. 백엔드에서 Base64 디코딩 → MultipartFile 변환
6. Supabase Storage에 업로드
7. 파일 URL 반환

---

## 프론트엔드 구현 (React + TypeScript)

### 1. Protobuf 설정

먼저 프로토콜 버퍼를 설치합니다:

\`\`\`bash
npm install protobufjs
npm install --save-dev @types/node
\`\`\`

### 2. Protobuf 메시지 정의 파일 생성

`proto/attachment.proto` 파일을 프론트엔드 프로젝트에 복사하고, protobufjs를 사용하여 TypeScript 코드를 생성합니다.

\`\`\`bash
npx pbjs -t static-module -w es6 -o src/proto/attachment.js proto/attachment.proto
npx pbts -o src/proto/attachment.d.ts src/proto/attachment.js
\`\`\`

### 3. 파일 업로드 컴포넌트

\`\`\`typescript
import React, { useState } from 'react';
import { attachment } from './proto/attachment'; // Protobuf 생성 파일

interface FileUploadProps {
  onUploadSuccess?: (fileUrl: string) => void;
  onUploadError?: (error: Error) => void;
}

export const FileUpload: React.FC<FileUploadProps> = ({ 
  onUploadSuccess, 
  onUploadError 
}) => {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  /**
   * 파일을 Base64로 인코딩
   */
  const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const base64String = (reader.result as string).split(',')[1]; // "data:image/png;base64," 제거
        resolve(base64String);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  };

  /**
   * Protobuf를 사용한 파일 업로드
   */
  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      setUploading(true);
      setProgress(0);

      // 1. 파일을 Base64로 인코딩
      console.log('Encoding file to Base64...');
      const base64Data = await fileToBase64(file);
      setProgress(30);

      // 2. Protobuf 메시지 생성
      const uploadRequest = attachment.AttachmentUploadRequest.create({
        fileName: file.name,
        contentType: file.type,
        fileDataBase64: base64Data,
        description: '사용자가 업로드한 파일',
        tags: ['user-upload']
      });

      // 3. Protobuf 메시지를 바이너리로 직렬화
      const buffer = attachment.AttachmentUploadRequest.encode(uploadRequest).finish();
      setProgress(50);

      // 4. 백엔드에 Protobuf 바이너리 전송
      console.log('Uploading to backend...', {
        fileName: file.name,
        fileSize: file.size,
        contentType: file.type,
        base64Length: base64Data.length,
        protobufSize: buffer.length
      });

      const response = await fetch('http://localhost:8080/api/attachments/upload', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-protobuf',
          // JWT 토큰이 필요한 경우
          // 'Authorization': \`Bearer \${token}\`
        },
        body: buffer
      });

      setProgress(80);

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(\`Upload failed: \${response.status} - \${errorText}\`);
      }

      // 5. JSON 응답 파싱 (Protobuf 응답으로 변경 가능)
      const result = await response.json();
      setProgress(100);

      console.log('Upload successful:', result);
      onUploadSuccess?.(result.file_url);

      // 성공 메시지 표시
      alert(\`파일이 성공적으로 업로드되었습니다!\nURL: \${result.file_url}\`);
    } catch (error) {
      console.error('Upload failed:', error);
      onUploadError?.(error as Error);
      alert(\`업로드 실패: \${(error as Error).message}\`);
    } finally {
      setUploading(false);
      setProgress(0);
    }
  };

  return (
    <div className="file-upload-container">
      <h2>Protobuf 파일 업로드</h2>
      
      <div className="upload-input">
        <input
          type="file"
          onChange={handleFileUpload}
          disabled={uploading}
          accept="image/jpeg,image/jpg,image/png,image/gif,image/webp,application/pdf"
        />
      </div>

      {uploading && (
        <div className="upload-progress">
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: \`\${progress}%\` }}
            />
          </div>
          <p>업로드 중... {progress}%</p>
        </div>
      )}
    </div>
  );
};
\`\`\`

### 4. Drag & Drop 파일 업로드 (고급)

\`\`\`typescript
import React, { useState, useCallback } from 'react';
import { attachment } from './proto/attachment';

export const DragDropFileUpload: React.FC = () => {
  const [dragging, setDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState<string[]>([]);

  const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const base64String = (reader.result as string).split(',')[1];
        resolve(base64String);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  };

  const uploadFile = async (file: File): Promise<string> => {
    const base64Data = await fileToBase64(file);

    const uploadRequest = attachment.AttachmentUploadRequest.create({
      fileName: file.name,
      contentType: file.type,
      fileDataBase64: base64Data
    });

    const buffer = attachment.AttachmentUploadRequest.encode(uploadRequest).finish();

    const response = await fetch('http://localhost:8080/api/attachments/upload', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-protobuf'
      },
      body: buffer
    });

    if (!response.ok) {
      throw new Error(\`Upload failed: \${response.status}\`);
    }

    const result = await response.json();
    return result.file_url;
  };

  const handleDrop = useCallback(async (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDragging(false);

    const files = Array.from(e.dataTransfer.files);
    if (files.length === 0) return;

    try {
      setUploading(true);

      // 여러 파일 동시 업로드
      const uploadPromises = files.map(file => uploadFile(file));
      const fileUrls = await Promise.all(uploadPromises);

      setUploadedFiles(prev => [...prev, ...fileUrls]);
      alert(\`\${files.length}개 파일 업로드 완료!\`);
    } catch (error) {
      console.error('Upload failed:', error);
      alert(\`업로드 실패: \${(error as Error).message}\`);
    } finally {
      setUploading(false);
    }
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDragging(false);
  }, []);

  return (
    <div>
      <div
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        style={{
          border: dragging ? '2px dashed #007bff' : '2px dashed #ccc',
          borderRadius: '8px',
          padding: '40px',
          textAlign: 'center',
          backgroundColor: dragging ? '#f0f8ff' : '#f9f9f9',
          cursor: 'pointer'
        }}
      >
        {uploading ? (
          <p>업로드 중...</p>
        ) : (
          <p>파일을 여기에 드래그 앤 드롭하세요</p>
        )}
      </div>

      {uploadedFiles.length > 0 && (
        <div style={{ marginTop: '20px' }}>
          <h3>업로드된 파일:</h3>
          <ul>
            {uploadedFiles.map((url, index) => (
              <li key={index}>
                <a href={url} target="_blank" rel="noopener noreferrer">
                  {url}
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};
\`\`\`

---

## 환경 변수 설정

### `.env` 파일 (백엔드)

\`\`\`bash
# Supabase Configuration
SUPABASE_URL=https://jxbmvvqjilxblzrojkek.supabase.co
SUPABASE_STORAGE_URL=https://jxbmvvqjilxblzrojkek.storage.supabase.co/storage/v1/s3
SUPABASE_REGION=ap-northeast-2
SUPABASE_ANON_KEY=your_anon_key_here
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key_here
SUPABASE_STORAGE_BUCKET=workfolio
SUPABASE_STORAGE_BASE_PATH=resumes/attachments
\`\`\`

### IntelliJ IDEA 환경 변수 설정

1. Run → Edit Configurations
2. Environment variables 섹션에 추가:
   \`\`\`
   SUPABASE_URL=https://jxbmvvqjilxblzrojkek.supabase.co;
   SUPABASE_STORAGE_URL=https://jxbmvvqjilxblzrojkek.storage.supabase.co/storage/v1/s3;
   SUPABASE_REGION=ap-northeast-2;
   SUPABASE_ANON_KEY=your_anon_key_here;
   SUPABASE_SERVICE_ROLE_KEY=your_service_role_key_here;
   SUPABASE_STORAGE_BUCKET=workfolio;
   SUPABASE_STORAGE_BASE_PATH=resumes/attachments
   \`\`\`

---

## 테스트

### 1. 백엔드 실행
\`\`\`bash
./gradlew bootRun
\`\`\`

### 2. 프론트엔드에서 파일 업로드 테스트

브라우저 개발자 도구의 Network 탭에서 다음을 확인:
- Request Headers: `Content-Type: application/x-protobuf`
- Request Payload: Binary (Protobuf)
- Response: JSON 형식의 업로드 결과

### 3. 예상 응답

\`\`\`json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "file_name": "profile.png",
  "file_url": "https://jxbmvvqjilxblzrojkek.supabase.co/storage/v1/object/public/workfolio/resumes/attachments/20251029140530_a1b2c3d4.png",
  "file_size": 245678,
  "content_type": "image/png",
  "created_at": "2025-10-29T14:05:30.123Z"
}
\`\`\`

---

## 주의사항

### 1. 파일 크기 제한
- 백엔드: 최대 10MB (AttachmentController에서 설정)
- Base64 인코딩 시 원본 파일보다 약 33% 크기 증가
- 네트워크 대역폭 고려 필요

### 2. 보안
- JWT 인증 토큰 필수 (프로덕션 환경)
- CORS 설정 확인
- 파일 타입 검증 (이미지, PDF 등만 허용)
- 파일 이름 sanitization

### 3. 성능 최적화
- 큰 파일은 청크 단위로 나누어 전송 고려
- WebSocket 또는 Server-Sent Events로 업로드 진행 상황 실시간 전달
- 이미지 파일은 클라이언트에서 리사이징 후 업로드

### 4. 에러 처리
- 네트워크 오류
- 파일 크기 초과
- 지원하지 않는 파일 형식
- Supabase Storage 업로드 실패

---

## 추가 기능

### 1. 다중 파일 업로드

프론트엔드에서 `Promise.all()`을 사용하여 여러 파일을 동시에 업로드할 수 있습니다.

### 2. 업로드 취소

`AbortController`를 사용하여 업로드를 중단할 수 있습니다.

\`\`\`typescript
const controller = new AbortController();

fetch('http://localhost:8080/api/attachments/upload', {
  method: 'POST',
  body: buffer,
  signal: controller.signal
});

// 취소
controller.abort();
\`\`\`

### 3. 이미지 미리보기

업로드 전에 클라이언트에서 이미지를 미리 보여줄 수 있습니다:

\`\`\`typescript
const previewUrl = URL.createObjectURL(file);
// <img src={previewUrl} alt="preview" />
\`\`\`

---

## 문제 해결

### Base64 디코딩 오류
- `data:image/png;base64,` 접두사를 제거했는지 확인
- Base64 문자열에 공백이나 줄바꿈이 없는지 확인

### CORS 오류
- 백엔드에서 CORS 설정 확인
- `WebMvcConfig`에서 `/api/attachments/**` 허용

### Protobuf 파싱 오류
- 프론트엔드와 백엔드의 `.proto` 파일이 동일한지 확인
- Protobuf 버전 일치 여부 확인

---

## 참고 자료
- [Protobuf.js Documentation](https://protobufjs.github.io/protobuf.js/)
- [Supabase Storage Documentation](https://supabase.com/docs/guides/storage)
- [Spring Boot WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)

