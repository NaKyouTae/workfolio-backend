package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.AttachmentCreateDto
import com.spectrum.workfolio.domain.dto.AttachmentUpdateDto
import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentTargetType
import com.spectrum.workfolio.domain.enums.AttachmentType
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import com.spectrum.workfolio.proto.attachment.AttachmentRequest
import com.spectrum.workfolio.utils.EntityTypeValidator
import com.spectrum.workfolio.utils.FileUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AttachmentCommandService(
    private val fileUploadService: FileUploadService,
    private val attachmentRepository: AttachmentRepository,
    private val attachmentQueryService: AttachmentQueryService,
) {

    private val logger = LoggerFactory.getLogger(AttachmentCommandService::class.java)

    @Transactional
    fun createAttachment(dto: AttachmentCreateDto): Attachment {
        // 먼저 Attachment 생성하여 ID 획득
        val attachment = Attachment(
            type = dto.type,
            category = dto.category,
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
        targetType: AttachmentTargetType,
        targetId: String,
        storagePath: String,
        requests: List<AttachmentRequest>,
    ): List<Attachment> {
        return requests.map { request ->
            val attachment = Attachment(
                type = AttachmentType.valueOf(request.type.name),
                category = AttachmentCategory.valueOf(request.category.name),
                fileName = request.fileName,
                fileUrl = "",
                url = request.url,
                isVisible = request.isVisible,
                priority = request.priority,
                targetId = targetId,
                targetType = targetType,
            )
            val savedAttachment = attachmentRepository.save(attachment)

            // fileData가 있으면 Supabase Storage에 업로드
            val uploadedFileUrl = if (request.hasFileData() && !request.fileData.isEmpty) {
                try {
                    val extension = FileUtil.extractFileExtension(request.fileName)
                    val storageFileName = "${savedAttachment.id}.$extension"

                    fileUploadService.uploadFileToStorage(
                        fileData = request.fileData,
                        fileName = storageFileName,
                        storagePath = "$storagePath/$targetId",
                    )
                } catch (e: Exception) {
                    attachmentRepository.delete(savedAttachment)
                    throw e
                }
            } else {
                request.fileUrl
            }

            savedAttachment.changeFileUrl(uploadedFileUrl)
            savedAttachment
        }
    }

    @Transactional
    fun createBulkAttachmentFromEntity(
        entity: Any,
        storagePath: String,
        attachments: List<Attachment>,
    ) {
        val resume = EntityTypeValidator.requireEntityType<Resume>(entity)

        attachments.map { originalAttachment ->
            val newAttachment = Attachment(
                fileName = originalAttachment.fileName,
                fileUrl = "",
                url = originalAttachment.url,
                isVisible = originalAttachment.isVisible,
                priority = originalAttachment.priority,
                type = originalAttachment.type,
                category = originalAttachment.category,
                targetId = originalAttachment.targetId,
                targetType = originalAttachment.targetType,
            )
            val savedAttachment = attachmentRepository.save(newAttachment)

            if (originalAttachment.fileUrl.isNotBlank()) {
                try {
                    val extension = originalAttachment.fileName.substringAfterLast(".", "")
                    val newFileName = "${savedAttachment.id}.$extension"

                    val copiedFileUrl = fileUploadService.copyFileInStorage(
                        sourceFileUrl = originalAttachment.fileUrl,
                        destinationFileName = newFileName,
                        destinationStoragePath = "$storagePath/${resume.id}",
                    )

                    savedAttachment.changeFileUrl(copiedFileUrl)
                } catch (e: Exception) {
                    logger.error("Failed to copy $originalAttachment ${e.message}")
                    savedAttachment.changeFileUrl(originalAttachment.fileUrl)
                }
            } else {
                logger.info("No file to copy for attachment: ${originalAttachment.id} (fileUrl is blank)")
            }

            savedAttachment
        }
    }

    @Transactional
    fun updateBulkAttachment(
        targetType: AttachmentTargetType,
        targetId: String,
        storagePath: String,
        requests: List<AttachmentRequest>,
    ): List<Attachment> {
        val existingAttachments = attachmentRepository.findByTargetIdAndTargetTypeOrderByPriorityAsc(targetId, targetType)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingAttachments.mapNotNull { attachment ->
            requestMap[attachment.id]?.let { request ->
                // fileData가 있으면 Supabase Storage에 업로드하고 기존 파일 삭제
                val uploadedFileUrl = if (request.hasFileData() && !request.fileData.isEmpty) {
                    val extension = FileUtil.extractFileExtension(request.fileName)
                    val storageFileName = "${attachment.id}.$extension"

                    val newFileUrl = fileUploadService.uploadFileToStorage(
                        fileData = request.fileData,
                        fileName = storageFileName,
                        storagePath = "$storagePath/$targetId",
                    )

                    fileUploadService.deleteFileFromStorage(listOf(attachment))
                    newFileUrl
                } else {
                    request.fileUrl
                }

                attachment.changeInfo(
                    type = AttachmentType.valueOf(request.type.name),
                    category = AttachmentCategory.valueOf(request.category.name),
                    fileName = request.fileName,
                    fileUrl = uploadedFileUrl,
                    url = request.url,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                attachment
            }
        }

        return attachmentRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun updateAttachment(dto: AttachmentUpdateDto): Attachment {
        val attachment = attachmentQueryService.getAttachment(dto.id)

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
            category = dto.category,
            fileName = dto.fileName ?: "",
            fileUrl = uploadedFileUrl,
            url = dto.url ?: "",
            isVisible = dto.isVisible,
            priority = dto.priority,
        )

        return attachment
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
}
