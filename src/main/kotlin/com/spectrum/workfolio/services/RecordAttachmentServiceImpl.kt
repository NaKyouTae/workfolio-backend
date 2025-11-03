package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.AttachmentCreateDto
import com.spectrum.workfolio.domain.dto.AttachmentUpdateDto
import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.entity.record.RecordAttachment
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.RecordAttachmentRepository
import com.spectrum.workfolio.utils.EntityTypeValidator.requireEntityType
import com.spectrum.workfolio.utils.FileUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordAttachmentServiceImpl(
    private val recordQueryService: RecordQueryService,
    private val fileUploadService: FileUploadService,
    private val recordAttachmentRepository: RecordAttachmentRepository,
) : AttachmentService<RecordAttachment> {

    private val logger = LoggerFactory.getLogger(RecordAttachmentServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun getAttachment(id: String): RecordAttachment {
        return recordAttachmentRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_ATTACHMENT.message) }
    }

    @Transactional(readOnly = true)
    override fun listAttachments(targetId: String): List<RecordAttachment> {
        return recordAttachmentRepository.findByRecordIdOrderByCreatedAtDesc(targetId)
    }

    @Transactional
    override fun createAttachment(dto: AttachmentCreateDto): RecordAttachment {
        val record = recordQueryService.getRecordEntity(dto.targetId)

        // 먼저 Attachment 생성하여 ID 획득
        val resumeAttachment = RecordAttachment(
            fileName = dto.fileName ?: "",
            fileUrl = "",
            record = record,
        )
        val savedAttachment = recordAttachmentRepository.save(resumeAttachment)

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
                recordAttachmentRepository.delete(savedAttachment)
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
    override fun createBulkAttachment(
        entity: Any,
        attachments: List<RecordAttachment>,
    ) {
        val record = requireEntityType<Record>(entity)
        
        val newAttachments = attachments.map { originalAttachment ->
            // 먼저 새 Attachment 생성 (ID 획득을 위해)
            val newResumeAttachment = RecordAttachment(
                fileName = originalAttachment.fileName,
                fileUrl = "", // 임시로 빈 값
                record = record,
            )
            val savedAttachment = recordAttachmentRepository.save(newResumeAttachment)

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
                        destinationStoragePath = "resumes/attachments/${record.id}",
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

        logger.info("Bulk created ${newAttachments.size} attachments for resume: ${record.id}")
    }

    @Transactional
    override fun updateAttachment(dto: AttachmentUpdateDto): RecordAttachment {
        val attachment = this.getAttachment(dto.id)

        // fileData가 있으면 Supabase Storage에 업로드하고 기존 파일 삭제
        val uploadedFileUrl = if (dto.fileData != null && !dto.fileData.isEmpty) {
            try {
                // 파일 확장자 추출
                val extension = FileUtil.extractFileExtension(dto.fileName ?: attachment.fileName)
                val storageFileName = "${attachment.id}.$extension"

                // 새 파일 업로드
                val newFileUrl = fileUploadService.uploadFileToStorage(
                    fileData = dto.fileData,
                    fileName = storageFileName,
                    storagePath = dto.storagePath,
                )

                // 업로드 성공 후 기존 파일 삭제
                fileUploadService.deleteFileFromStorage(listOf(attachment))

                newFileUrl
            } catch (e: Exception) {
                // 업로드 실패 시 기존 파일 유지
                logger.error("Failed to upload new file, keeping old file", e)
                throw e
            }
        } else {
            dto.fileUrl ?: attachment.fileUrl
        }

        attachment.changeInfo(
            fileName = dto.fileName ?: "",
            fileUrl = uploadedFileUrl,
        )

        return attachment
    }

    @Transactional
    override fun deleteAttachment(id: String) {
        val attachment = this.getAttachment(id)

        fileUploadService.deleteFileFromStorage(listOf(attachment))
        recordAttachmentRepository.delete(attachment)
    }

    @Transactional
    override fun deleteAttachments(attachmentIds: List<String>) {
        if (attachmentIds.isEmpty()) {
            return
        }

        // ID로 Attachment 조회하여 파일 URL 가져오기
        val attachments = recordAttachmentRepository.findAllById(attachmentIds)

        fileUploadService.deleteFileFromStorage(attachments)

        // DB에서 Attachment 삭제
        recordAttachmentRepository.deleteAllById(attachmentIds)
    }

    @Transactional
    override fun deleteAttachmentsByTargetId(targetId: String) {
        val attachments = recordAttachmentRepository.findByRecordIdOrderByCreatedAtDesc(targetId)
        fileUploadService.deleteFileFromStorage(attachments)
        recordAttachmentRepository.deleteAll(attachments)
    }
}
