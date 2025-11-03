package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.AttachmentCreateDto
import com.spectrum.workfolio.domain.dto.AttachmentUpdateDto
import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import com.spectrum.workfolio.utils.EntityTypeValidator
import com.spectrum.workfolio.utils.FileUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AttachmentCommandService(
    private val fileUploadService: FileUploadService,
    private val attachmentRepository: AttachmentRepository,
) {

    private val logger = LoggerFactory.getLogger(AttachmentCommandService::class.java)

    @Transactional(readOnly = true)
    fun getAttachment(id: String): Attachment {
        return attachmentRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_ATTACHMENT.message)
        }
    }

    @Transactional(readOnly = true)
    fun listAttachments(targetId: String): List<Attachment> {
        return attachmentRepository.findByTargetIdOrderByPriorityAsc(targetId)
    }

    @Transactional(readOnly = true)
    fun listAttachments(targetIds: List<String>): List<Attachment> {
        return attachmentRepository.findByTargetIdInOrderByPriorityAsc(targetIds)
    }

    @Transactional
    fun createAttachment(dto: AttachmentCreateDto): Attachment {
        // 먼저 Attachment 생성하여 ID 획득
        val attachment = Attachment(
            type = dto.type,
            category = dto.category ?: AttachmentCategory.FILE,
            fileName = dto.fileName ?: "",
            fileUrl = "",
            url = dto.url ?: "",
            isVisible = dto.isVisible,
            priority = dto.priority,
            targetId = dto.targetId,
            targetType = dto.targetType,
        )
        val savedAttachment = attachmentRepository.save(attachment)

        // fileData가 있으면 Supabase Storage에 업로드
        val uploadedFileUrl = if (dto.fileData != null && !dto.fileData.isEmpty) {
            try {
                // 파일 확장자 추출
                val extension = FileUtil.extractFileExtension(dto.fileName)
                val storageFileName = "${savedAttachment.id}.$extension"

                fileUploadService.uploadFileToStorage(
                    fileData = dto.fileData,
                    fileName = storageFileName,
                    storagePath = dto.storagePath,
                )
            } catch (e: Exception) {
                // 업로드 실패 시 생성된 Attachment 삭제 (롤백)
                attachmentRepository.delete(savedAttachment)
                throw e
            }
        } else {
            dto.fileUrl ?: ""
        }

        // fileUrl 업데이트 (Dirty Checking으로 자동 저장)
        savedAttachment.changeFileUrl(uploadedFileUrl)

        return savedAttachment
    }

    @Transactional
    fun createBulkAttachment(
        entity: Any,
        attachments: List<Attachment>,
    ) {
        val resume = EntityTypeValidator.requireEntityType<Resume>(entity)

        attachments.map { originalAttachment ->
            // 먼저 새 Attachment 생성 (ID 획득을 위해)
            val newAttachment = Attachment(
                fileName = originalAttachment.fileName,
                fileUrl = "", // 임시로 빈 값
                url = originalAttachment.url,
                isVisible = originalAttachment.isVisible,
                priority = originalAttachment.priority,
                type = originalAttachment.type,
                category = originalAttachment.category,
                targetId = originalAttachment.targetId,
                targetType = originalAttachment.targetType,
            )
            val savedAttachment = attachmentRepository.save(newAttachment)

            // 원본에 파일이 있으면 Storage에서 복사
            if (originalAttachment.fileUrl.isNotBlank()) {
                try {
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

                    savedAttachment.changeFileUrl(copiedFileUrl)
                } catch (e: Exception) {
                    savedAttachment.changeFileUrl(originalAttachment.fileUrl)
                }
            } else {
                logger.info("No file to copy for attachment: ${originalAttachment.id} (fileUrl is blank)")
            }

            savedAttachment
        }
    }

    @Transactional
    fun updateAttachment(dto: AttachmentUpdateDto): Attachment {
        val attachment = this.getAttachment(dto.id)

        // fileData가 있으면 Supabase Storage에 업로드하고 기존 파일 삭제
        val uploadedFileUrl = if (dto.fileData != null && !dto.fileData.isEmpty) {
            // 파일 확장자 추출
            val extension = FileUtil.extractFileExtension(dto.fileName ?: attachment.fileName)
            val storageFileName = "${attachment.id}.$extension"

            // 새 파일 업로드
            val newFileUrl = fileUploadService.uploadFileToStorage(
                fileData = dto.fileData,
                fileName = storageFileName,
                storagePath = dto.storagePath,
            )

            fileUploadService.deleteFileFromStorage(listOf(attachment))

            newFileUrl
        } else {
            dto.fileUrl ?: attachment.fileUrl
        }

        attachment.changeInfo(
            type = dto.type,
            category = dto.category ?: AttachmentCategory.FILE,
            fileName = dto.fileName ?: "",
            fileUrl = uploadedFileUrl,
            url = dto.url ?: "",
            isVisible = dto.isVisible,
            priority = dto.priority,
        )

        return attachment
    }

    @Transactional
    fun deleteAttachment(id: String) {
        val attachment = this.getAttachment(id)

        fileUploadService.deleteFileFromStorage(listOf(attachment))

        attachmentRepository.delete(attachment)
    }

    @Transactional
    fun deleteAttachments(attachmentIds: List<String>) {
        if (attachmentIds.isEmpty()) {
            return
        }

        // ID로 Attachment 조회하여 파일 URL 가져오기
        val attachments = attachmentRepository.findAllById(attachmentIds)

        fileUploadService.deleteFileFromStorage(attachments)

        // DB에서 Attachment 삭제
        attachmentRepository.deleteAllById(attachmentIds)
    }

    @Transactional
    fun deleteAttachmentsByTargetId(targetId: String) {
        val attachments = attachmentRepository.findByTargetIdOrderByPriorityAsc(targetId)

        fileUploadService.deleteFileFromStorage(attachments)

        // DB에서 Attachment 삭제
        attachmentRepository.deleteAll(attachments)
    }
}
