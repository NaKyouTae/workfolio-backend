package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AttachmentQueryService(
    private val attachmentRepository: AttachmentRepository,
) {
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
}
