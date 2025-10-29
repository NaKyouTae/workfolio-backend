package com.spectrum.workfolio.services

import com.spectrum.workfolio.config.SupabaseConfig
import com.spectrum.workfolio.utils.WorkfolioException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Service
class SupabaseStorageService(
    private val s3Client: S3Client,
    private val supabaseConfig: SupabaseConfig,
) {
    private val bucket = "workfolio"
    private val logger = LoggerFactory.getLogger(SupabaseStorageService::class.java)

    init {
        logger.info("Supabase Storage (AWS SDK 2.x) initialized:")
        logger.info("  - Base URL: ${supabaseConfig.url}")
        logger.info("  - Storage URL: ${supabaseConfig.storageUrl}")
        logger.info("  - Region: ${supabaseConfig.region}")
        logger.info("  - Bucket: $bucket")
    }

    /**
     * Supabase Storage에 파일 업로드 (AWS S3 SDK 2.x 사용)
     * @param file 업로드할 파일
     * @param storagePath 저장할 전체 경로 (예: "resumes/attachments/resumeId")
     * @return 업로드된 파일의 public URL
     */
    fun uploadFile(file: MultipartFile, fileName: String, storagePath: String): String {
        try {
            val filePath = "$storagePath/$fileName"

            logger.info(
                "Uploading file to Supabase Storage (S3 v2): " +
                    "bucket=$bucket, key=$filePath",
            )

            // S3 업로드 요청 생성
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filePath)
                .contentType(file.contentType ?: "application/octet-stream")
                .contentLength(file.size)
                .acl(ObjectCannedACL.PUBLIC_READ) // Public 접근 권한 설정
                .build()

            // RequestBody 생성
            val requestBody = RequestBody.fromInputStream(file.inputStream, file.size)

            // S3에 파일 업로드
            val result = s3Client.putObject(putObjectRequest, requestBody)
            logger.info("File uploaded successfully: key=$filePath, etag=${result.eTag()}")

            // Public URL 반환
            return getPublicUrl(filePath)
        } catch (e: Exception) {
            logger.error("Failed to upload file to Supabase Storage (S3 v2)", e)
            throw WorkfolioException("파일 업로드 중 오류가 발생했습니다: ${e.message}")
        }
    }

    /**
     * Supabase Storage에서 파일 삭제 (AWS S3 SDK 2.x 사용)
     * @param filePath 삭제할 파일의 경로 (bucket 내 상대 경로)
     */
    fun deleteFile(filePath: String) {
        try {
            logger.info(
                "Deleting file from Supabase Storage (S3 v2): " +
                    "bucket=$bucket, key=$filePath",
            )

            val deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(filePath)
                .build()

            s3Client.deleteObject(deleteObjectRequest)
            logger.info("File deleted successfully: $filePath")
        } catch (e: Exception) {
            logger.error("Failed to delete file from Supabase Storage (S3 v2)", e)
            throw WorkfolioException("파일 삭제 중 오류가 발생했습니다: ${e.message}")
        }
    }

    /**
     * URL에서 파일 경로 추출 후 삭제
     * @param fileUrl Public URL
     */
    fun deleteFileByUrl(fileUrl: String) {
        val filePath = extractFilePathFromUrl(fileUrl)
        deleteFile(filePath)
    }

    /**
     * Public URL 생성
     * @param filePath bucket 내 파일 경로
     * @return Public URL
     */
    fun getPublicUrl(filePath: String): String {
        return "${supabaseConfig.url}/storage/v1/object/public/$bucket/$filePath"
    }

    /**
     * URL에서 파일 경로 추출
     * @param fileUrl Public URL
     * @return bucket 내 파일 경로
     */
    private fun extractFilePathFromUrl(fileUrl: String): String {
        val prefix = "${supabaseConfig.url}/storage/v1/object/public/$bucket/"
        return fileUrl.removePrefix(prefix)
    }
}
