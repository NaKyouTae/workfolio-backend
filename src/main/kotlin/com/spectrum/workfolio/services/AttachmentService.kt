package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.AttachmentCreateDto
import com.spectrum.workfolio.domain.dto.AttachmentUpdateDto
import com.spectrum.workfolio.domain.entity.BaseAttachment

interface AttachmentService<T : BaseAttachment> {
    fun getAttachment(id: String): T
    fun listAttachments(targetId: String): List<T>

    fun createAttachment(dto: AttachmentCreateDto): T
    fun createBulkAttachment(entity: Any, attachments: List<T>)

    fun updateAttachment(dto: AttachmentUpdateDto): T

    fun deleteAttachment(id: String)
    fun deleteAttachments(attachmentIds: List<String>)
    fun deleteAttachmentsByTargetId(targetId: String)
}
