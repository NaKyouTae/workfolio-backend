package com.spectrum.workfolio.services

import com.google.protobuf.ByteString
import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.utils.Base64MultipartFile
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class FileUploadService(
    private val supabaseStorageService: SupabaseStorageService,
) {
    companion object {
        private const val MAX_FILE_SIZE = 10L * 1024 * 1024 // 10MB
        private val ALLOWED_EXTENSIONS = setOf(
            "pdf", "doc", "docx", "xlsx", "pptx", "hwp", "txt",
            "png", "jpg", "jpeg", "gif", "webp",
        )
    }

    fun uploadFileToStorage(
        fileData: ByteString,
        fileName: String,
        storagePath: String,
    ): String {
        try {
            // 파일 크기 검증
            if (fileData.size() > MAX_FILE_SIZE) {
                throw WorkfolioException("파일 크기는 10MB 이하만 가능합니다.")
            }

            // 파일 확장자 검증
            val extension = fileName.substringAfterLast(".", "").lowercase()
            if (extension.isBlank() || extension !in ALLOWED_EXTENSIONS) {
                throw WorkfolioException("허용되지 않는 파일 형식입니다. (허용: ${ALLOWED_EXTENSIONS.joinToString(", ")})")
            }

            // ByteString을 Base64로 인코딩
            val base64Data = Base64.getEncoder().encodeToString(fileData.toByteArray())

            // MIME 타입 추론 (확장자 기반)
            val contentType = when (extension) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "pdf" -> "application/pdf"
                "doc" -> "application/msword"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                "hwp" -> "application/x-hwp"
                "txt" -> "text/plain"
                else -> "application/octet-stream"
            }

            // Base64MultipartFile 생성
            val multipartFile = Base64MultipartFile(
                base64Data = base64Data,
                fileName = fileName,
                contentType = contentType,
            )

            // Supabase Storage에 업로드
            return supabaseStorageService.uploadFile(
                file = multipartFile,
                fileName = fileName,
                storagePath = storagePath,
            )
        } catch (e: Exception) {
            throw WorkfolioException("파일 업로드 중 오류가 발생했습니다: ${e.message}")
        }
    }

    fun deleteFileFromStorage(attachments: List<Attachment>) {
        attachments.forEach { attachment ->
            if (attachment.fileUrl.isNotBlank()) {
                supabaseStorageService.deleteFileByUrl(attachment.fileUrl)
            }
        }
    }

    /**
     * Storage에서 파일 복사
     * @param sourceFileUrl 원본 파일 URL
     * @param destinationFileName 새로운 파일 이름
     * @param destinationStoragePath 새로운 저장 경로
     * @return 복사된 파일의 URL
     */
    fun copyFileInStorage(
        sourceFileUrl: String,
        destinationFileName: String,
        destinationStoragePath: String,
    ): String {
        return supabaseStorageService.copyFile(sourceFileUrl, destinationFileName, destinationStoragePath)
    }
}
