package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.common.Attachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttachmentRepository : JpaRepository<Attachment, String> {
    fun findByTargetIdOrderByPriorityAsc(targetId: String): List<Attachment>
    fun findByTargetIdInOrderByPriorityAsc(targetIds: List<String>): List<Attachment>
}
