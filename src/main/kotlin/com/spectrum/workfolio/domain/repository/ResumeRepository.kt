package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Resume
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ResumeRepository : JpaRepository<Resume, String> {
    fun findByWorkerIdOrderByIsDefaultDescUpdatedAtDesc(workerId: String): List<Resume>
    fun findByWorkerIdOrderByIsDefaultDescUpdatedAtDesc(workerId: String, pageable: Pageable): Page<Resume>

    @Query(
        """
        SELECT r FROM Resume r
        WHERE r.publicId = :publicId
        AND r.isPublic = true
        AND (r.publicStartDate IS NULL OR r.publicStartDate <= :today)
        AND (r.publicEndDate IS NULL OR r.publicEndDate >= :today)
        """
    )
    fun findByPublicIdAndIsPublicTrue(
        @Param("publicId") publicId: String,
        @Param("today") today: LocalDate
    ): Resume?

    @Modifying
    @Query(
        value = """
        UPDATE Resume r
        SET r.isDefault = false
        WHERE r.worker.id = :workerId
        AND r.id != :excludeId
        AND r.isDefault = true
    """,
    )
    fun updateAllDefaultToFalse(workerId: String, excludeId: String)
}
