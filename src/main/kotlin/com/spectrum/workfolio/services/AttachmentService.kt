package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Attachment
import com.spectrum.workfolio.domain.enums.AttachmentType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AttachmentService(
    private val resumeQueryService: ResumeQueryService,
    private val attachmentRepository: AttachmentRepository,
) {

    @Transactional(readOnly = true)
    fun getAttachment(id: String): Attachment {
        return attachmentRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_ATTACHMENT.message) }
    }

    @Transactional(readOnly = true)
    fun listAttachments(resumeId: String): List<Attachment> {
        return attachmentRepository.findByResumeId(resumeId)
    }

    @Transactional
    fun createAttachment(
        resumeId: String,
        type: AttachmentType? = null,
        fileName: String? = null,
        fileUrl: String? = null,
        isVisible: Boolean,
    ): Attachment {
        val resume = resumeQueryService.getResume(resumeId)
        val attachment = Attachment(
            type = type,
            fileName = fileName ?: "",
            fileUrl = fileUrl ?: "",
            isVisible = isVisible,
            resume = resume,
        )

        return attachmentRepository.save(attachment)
    }

    @Transactional
    fun updateAttachment(
        id: String,
        type: AttachmentType?,
        fileName: String?,
        fileUrl: String?,
        isVisible: Boolean,
    ): Attachment {
        val attachment = this.getAttachment(id)

        attachment.changeInfo(
            type = type,
            fileName = fileName ?: "",
            fileUrl = fileUrl ?: "",
            isVisible = isVisible,
        )

        return attachmentRepository.save(attachment)
    }

    @Transactional
    fun deleteAttachment(id: String) {
        val attachment = this.getAttachment(id)
        attachmentRepository.delete(attachment)
    }

    @Transactional
    fun deleteAttachments(attachmentIds: List<String>) {
        if (attachmentIds.isNotEmpty()) {
            attachmentRepository.deleteAllById(attachmentIds)
        }
    }

    @Transactional
    fun deleteAttachmentsByResumeId(resumeId: String) {
        val attachments = attachmentRepository.findByResumeId(resumeId)
        attachmentRepository.deleteAll(attachments)
    }
}
