package com.spectrum.workfolio.services

import com.fasterxml.jackson.databind.JsonSerializable
import com.google.protobuf.ByteString
import com.spectrum.workfolio.domain.entity.BaseAttachment
import com.spectrum.workfolio.utils.Base64MultipartFile
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class FileUploadService(
    private val supabaseStorageService: SupabaseStorageService,
) {
    fun uploadFileToStorage(
        fileData: ByteString,
        fileName: String,
        storagePath: String,
    ): String {
        try {
            // ByteString을 Base64로 인코딩
            val base64Data = Base64.getEncoder().encodeToString(fileData.toByteArray())

            // MIME 타입 추론 (확장자 기반)
            val contentType = when (fileName.substringAfterLast(".", "").lowercase()) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "pdf" -> "application/pdf"
                "doc" -> "application/msword"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
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

    fun deleteFileFromStorage(attachments: List<BaseAttachment>) {
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
