package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.record.RecordAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RecordAttachmentRepository : JpaRepository<RecordAttachment, String> {
    fun findByRecordIdOrderByCreatedAtDesc(recordId: String): List<RecordAttachment>

    @Query("SELECT a FROM RecordAttachment a WHERE a.record.worker.id = :workerId ORDER BY a.createdAt DESC")
    fun findByWorkerId(@Param("workerId") workerId: String): List<RecordAttachment>
}
