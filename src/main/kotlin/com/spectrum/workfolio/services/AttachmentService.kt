package com.spectrum.workfolio.services

import com.google.protobuf.ByteString
import com.spectrum.workfolio.domain.entity.resume.Attachment
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AttachmentService(
    private val fileUploadService: FileUploadService,
    private val resumeQueryService: ResumeQueryService,
    private val attachmentRepository: AttachmentRepository,

) {
    private val logger = LoggerFactory.getLogger(AttachmentService::class.java)

    @Transactional(readOnly = true)
    fun getAttachment(id: String): Attachment {
        return attachmentRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_ATTACHMENT.message) }
    }

    @Transactional(readOnly = true)
    fun listAttachments(resumeId: String): List<Attachment> {
        return attachmentRepository.findByResumeIdOrderByPriorityAsc(resumeId)
    }

    @Transactional
    fun createAttachment(
        resumeId: String,
        type: AttachmentType? = null,
        category: AttachmentCategory,
        fileName: String? = null,
        fileUrl: String? = null,
        url: String? = null,
        fileData: ByteString? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): Attachment {
        val resume = resumeQueryService.getResume(resumeId)

        // 먼저 Attachment 생성하여 ID 획득
        val attachment = Attachment(
            type = type,
            category = category,
            fileName = fileName ?: "",
            fileUrl = "",
            url = url ?: "",
            isVisible = isVisible,
            priority = priority,
            resume = resume,
        )
        val savedAttachment = attachmentRepository.save(attachment)

        // fileData가 있으면 Supabase Storage에 업로드
        val uploadedFileUrl = if (fileData != null && !fileData.isEmpty) {
            try {
                // 파일 확장자 추출
                val extension = extractFileExtension(fileName)
                val storageFileName = "${savedAttachment.id}.$extension"

                fileUploadService.uploadFileToStorage(
                    fileData = fileData,
                    fileName = storageFileName,
                    storagePath = "resumes/attachments/$resumeId",
                )
            } catch (e: Exception) {
                // 업로드 실패 시 생성된 Attachment 삭제 (롤백)
                attachmentRepository.delete(savedAttachment)
                throw e
            }
        } else {
            fileUrl ?: ""
        }

        // fileUrl 업데이트 (Dirty Checking으로 자동 저장)
        savedAttachment.changeFileUrl(uploadedFileUrl)

        return savedAttachment
    }

    @Transactional
    fun createBulkAttachment(
        resume: Resume,
        attachments: List<Attachment>,
    ) {
        val newAttachments = attachments.map { originalAttachment ->
            // 먼저 새 Attachment 생성 (ID 획득을 위해)
            val newAttachment = Attachment(
                fileName = originalAttachment.fileName,
                fileUrl = "", // 임시로 빈 값
                url = originalAttachment.url,
                isVisible = originalAttachment.isVisible,
                priority = originalAttachment.priority,
                type = originalAttachment.type,
                category = originalAttachment.category,
                resume = resume,
            )
            val savedAttachment = attachmentRepository.save(newAttachment)

            // 원본에 파일이 있으면 Storage에서 복사
            if (originalAttachment.fileUrl.isNotBlank()) {
                try {
                    logger.info(
                        "Starting file copy for attachment: " +
                            "originalId=${originalAttachment.id}, " +
                            "newId=${savedAttachment.id}, " +
                            "sourceUrl=${originalAttachment.fileUrl}",
                    )

                    // 원본 파일명에서 확장자 추출
                    val extension = originalAttachment.fileName.substringAfterLast(".", "")
                    // 새로운 파일명 생성: {새AttachmentId}.{확장자}
                    val newFileName = "${savedAttachment.id}.$extension"

                    // Storage에서 파일 복사
                    val copiedFileUrl = fileUploadService.copyFileInStorage(
                        sourceFileUrl = originalAttachment.fileUrl,
                        destinationFileName = newFileName,
                        destinationStoragePath = "resumes/attachments/${resume.id}",
                    )

                    // 복사된 파일 URL로 업데이트
                    savedAttachment.changeFileUrl(copiedFileUrl)

                    logger.info(
                        "File copied successfully for attachment: " +
                            "originalUrl=${originalAttachment.fileUrl}, " +
                            "newUrl=$copiedFileUrl",
                    )
                } catch (e: Exception) {
                    logger.error(
                        "Failed to copy file for attachment: " +
                            "originalId=${originalAttachment.id}, " +
                            "newId=${savedAttachment.id}, " +
                            "error=${e.message}",
                        e,
                    )
                    // 파일 복사 실패 시 원본 URL을 그대로 사용 (fallback)
                    savedAttachment.changeFileUrl(originalAttachment.fileUrl)
                }
            } else {
                logger.info("No file to copy for attachment: ${originalAttachment.id} (fileUrl is blank)")
            }

            savedAttachment
        }

        logger.info("Bulk created ${newAttachments.size} attachments for resume: ${resume.id}")
    }

    @Transactional
    fun updateAttachment(
        id: String,
        type: AttachmentType? = null,
        category: AttachmentCategory,
        fileName: String? = null,
        fileUrl: String? = null,
        url: String? = null,
        fileData: ByteString? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): Attachment {
        val attachment = this.getAttachment(id)

        // fileData가 있으면 Supabase Storage에 업로드하고 기존 파일 삭제
        val uploadedFileUrl = if (fileData != null && !fileData.isEmpty) {
            var oldFileUrl: String? = null
            try {
                // 기존 파일 URL 백업
                oldFileUrl = attachment.fileUrl.takeIf { it.isNotBlank() }

                // 파일 확장자 추출
                val extension = extractFileExtension(fileName ?: attachment.fileName)
                val storageFileName = "${attachment.id}.$extension"

                // 새 파일 업로드
                val newFileUrl = fileUploadService.uploadFileToStorage(
                    fileData = fileData,
                    fileName = storageFileName,
                    storagePath = "resumes/attachments/${attachment.resume.id}",
                )

                // 업로드 성공 후 기존 파일 삭제
                oldFileUrl?.let {
                    try {
                        fileUploadService.deleteFileFromStorage(it)
                    } catch (e: Exception) {
                        logger.warn("Failed to delete old file: $it", e)
                    }
                }

                newFileUrl
            } catch (e: Exception) {
                // 업로드 실패 시 기존 파일 유지
                logger.error("Failed to upload new file, keeping old file", e)
                throw e
            }
        } else {
            fileUrl ?: attachment.fileUrl
        }

        attachment.changeInfo(
            type = type,
            category = category,
            fileName = fileName ?: "",
            fileUrl = uploadedFileUrl,
            url = url ?: "",
            isVisible = isVisible,
            priority = priority,
        )

        return attachment
    }

    @Transactional
    fun deleteAttachment(id: String) {
        val attachment = this.getAttachment(id)

        // Supabase Storage에서 파일 삭제
        if (attachment.fileUrl.isNotBlank()) {
            try {
                fileUploadService.deleteFileFromStorage(attachment.fileUrl)
            } catch (e: Exception) {
                logger.warn("Failed to delete file from storage: ${attachment.fileUrl}", e)
            }
        }

        attachmentRepository.delete(attachment)
    }

    /**
     * 파일 확장자 추출
     * @param fileName 원본 파일 이름 (예: "portfolio.pdf")
     * @return 확장자 (예: "pdf"), 없으면 "bin"
     */
    private fun extractFileExtension(fileName: String?): String {
        if (fileName.isNullOrBlank()) return "bin"

        val extension = fileName.substringAfterLast(".", "")
        return if (extension.isNotBlank() && extension.length <= 10) {
            extension.lowercase()
        } else {
            "bin"
        }
    }

    @Transactional
    fun deleteAttachments(attachmentIds: List<String>) {
        if (attachmentIds.isEmpty()) {
            return
        }

        // ID로 Attachment 조회하여 파일 URL 가져오기
        val attachments = attachmentRepository.findAllById(attachmentIds)

        // Supabase Storage에서 파일 삭제
        attachments.forEach { attachment ->
            if (attachment.fileUrl.isNotBlank()) {
                try {
                    fileUploadService.deleteFileFromStorage(attachment.fileUrl)
                    logger.info("File deleted from storage: ${attachment.fileUrl}")
                } catch (e: Exception) {
                    logger.warn("Failed to delete file from storage: ${attachment.fileUrl}", e)
                }
            }
        }

        // DB에서 Attachment 삭제
        attachmentRepository.deleteAllById(attachmentIds)
    }

    @Transactional
    fun deleteAttachmentsByResumeId(resumeId: String) {
        val attachments = attachmentRepository.findByResumeIdOrderByPriorityAsc(resumeId)

        // Supabase Storage에서 파일 삭제
        attachments.forEach { attachment ->
            if (attachment.fileUrl.isNotBlank()) {
                try {
                    fileUploadService.deleteFileFromStorage(attachment.fileUrl)
                    logger.info("File deleted from storage: ${attachment.fileUrl}")
                } catch (e: Exception) {
                    logger.warn("Failed to delete file from storage: ${attachment.fileUrl}", e)
                }
            }
        }

        // DB에서 Attachment 삭제
        attachmentRepository.deleteAll(attachments)
    }
}
