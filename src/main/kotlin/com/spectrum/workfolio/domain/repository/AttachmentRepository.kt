package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Attachment
import com.spectrum.workfolio.domain.enums.AttachmentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AttachmentRepository : JpaRepository<Attachment, String> {

    fun findByResumeId(resumeId: String): List<Attachment>

    @Query("SELECT a FROM Attachment a WHERE a.resume.worker.id = :workerId")
    fun findByWorkerId(@Param("workerId") workerId: String): List<Attachment>

    @Query("SELECT a FROM Attachment a WHERE a.resume.id = :resumeId AND a.isVisible = true")
    fun findVisibleByResumeId(@Param("resumeId") resumeId: String): List<Attachment>

    fun findByResumeIdAndType(resumeId: String, type: AttachmentType): List<Attachment>
}
