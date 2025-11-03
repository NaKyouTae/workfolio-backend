package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.AttachmentCreateDto
import com.spectrum.workfolio.domain.dto.AttachmentUpdateDto
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.entity.resume.ResumeAttachment
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.ResumeAttachmentRepository
import com.spectrum.workfolio.interfaces.AttachmentService
import com.spectrum.workfolio.utils.EntityTypeValidator.requireEntityType
import com.spectrum.workfolio.utils.FileUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResumeAttachmentServiceImpl(
    private val fileUploadService: FileUploadService,
    private val resumeQueryService: ResumeQueryService,
    private val resumeAttachmentRepository: ResumeAttachmentRepository,
) : AttachmentService<ResumeAttachment> {

    private val logger = LoggerFactory.getLogger(ResumeAttachmentServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun getAttachment(id: String): ResumeAttachment {
        return resumeAttachmentRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_ATTACHMENT.message)
        }
    }

    @Transactional(readOnly = true)
    override fun listAttachments(targetId: String): List<ResumeAttachment> {
        return resumeAttachmentRepository.findByResumeIdOrderByPriorityAsc(targetId)
    }

    @Transactional
    override fun createAttachment(dto: AttachmentCreateDto): ResumeAttachment {
        val resume = resumeQueryService.getResume(dto.targetId)

        // 먼저 Attachment 생성하여 ID 획득
        val resumeAttachment = ResumeAttachment(
            type = dto.type,
            category = dto.category ?: AttachmentCategory.FILE,
            fileName = dto.fileName ?: "",
            fileUrl = "",
            url = dto.url ?: "",
            isVisible = dto.isVisible,
            priority = dto.priority,
            resume = resume,
        )
        val savedAttachment = resumeAttachmentRepository.save(resumeAttachment)

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
                resumeAttachmentRepository.delete(savedAttachment)
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
        attachments: List<ResumeAttachment>,
    ) {
        val resume = requireEntityType<Resume>(entity)

        attachments.map { originalAttachment ->
            // 먼저 새 Attachment 생성 (ID 획득을 위해)
            val newResumeAttachment = ResumeAttachment(
                fileName = originalAttachment.fileName,
                fileUrl = "", // 임시로 빈 값
                url = originalAttachment.url,
                isVisible = originalAttachment.isVisible,
                priority = originalAttachment.priority,
                type = originalAttachment.type,
                category = originalAttachment.category,
                resume = resume,
            )
            val savedAttachment = resumeAttachmentRepository.save(newResumeAttachment)

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
    override fun updateAttachment(dto: AttachmentUpdateDto): ResumeAttachment {
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

                fileUploadService.deleteFileFromStorage(listOf(attachment))

                newFileUrl
            } catch (e: Exception) {
                // 업로드 실패 시 기존 파일 유지
                throw e
            }
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
    override fun deleteAttachment(id: String) {
        val attachment = this.getAttachment(id)

        fileUploadService.deleteFileFromStorage(listOf(attachment))

        resumeAttachmentRepository.delete(attachment)
    }

    @Transactional
    override fun deleteAttachments(attachmentIds: List<String>) {
        if (attachmentIds.isEmpty()) {
            return
        }

        // ID로 Attachment 조회하여 파일 URL 가져오기
        val attachments = resumeAttachmentRepository.findAllById(attachmentIds)

        fileUploadService.deleteFileFromStorage(attachments)

        // DB에서 Attachment 삭제
        resumeAttachmentRepository.deleteAllById(attachmentIds)
    }

    @Transactional
    override fun deleteAttachmentsByTargetId(targetId: String) {
        val attachments = resumeAttachmentRepository.findByResumeIdOrderByPriorityAsc(targetId)

        fileUploadService.deleteFileFromStorage(attachments)

        // DB에서 Attachment 삭제
        resumeAttachmentRepository.deleteAll(attachments)
    }
}
