package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.ResumeAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ResumeAttachmentRepository : JpaRepository<ResumeAttachment, String> {

    fun findByResumeIdOrderByPriorityAsc(resumeId: String): List<ResumeAttachment>

    @Query("SELECT a FROM ResumeAttachment a WHERE a.resume.worker.id = :workerId ORDER BY a.priority ASC")
    fun findByWorkerId(@Param("workerId") workerId: String): List<ResumeAttachment>
}
