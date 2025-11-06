package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Resume
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ResumeRepository : JpaRepository<Resume, String> {
    fun findByWorkerIdOrderByIsDefaultDescUpdatedAtDesc(workerId: String): List<Resume>

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
