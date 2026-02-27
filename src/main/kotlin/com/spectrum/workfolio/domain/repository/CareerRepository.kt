package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Career
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CareerRepository : JpaRepository<Career, String> {
    fun findByResumeIdOrderByPriorityAscStartedAtDescEndedAtDesc(resumeId: String): List<Career>

    @Query(
        """
        SELECT c
          FROM Career c
          JOIN c.resume r
         WHERE r.worker.id = :workerId
         ORDER BY c.startedAt DESC, c.endedAt DESC
        """,
        countQuery = """
        SELECT COUNT(c)
          FROM Career c
          JOIN c.resume r
         WHERE r.worker.id = :workerId
        """,
    )
    fun findAdminCareersByWorkerId(
        @Param("workerId") workerId: String,
        pageable: Pageable,
    ): Page<Career>

    @Query(
        """
        SELECT c
          FROM Career c
         WHERE c.resume.id IN :resumeIds
         ORDER BY c.startedAt DESC, c.endedAt DESC
        """,
        countQuery = """
        SELECT COUNT(c)
          FROM Career c
         WHERE c.resume.id IN :resumeIds
        """,
    )
    fun findAdminCareersByResumeIds(
        @Param("resumeIds") resumeIds: List<String>,
        pageable: Pageable,
    ): Page<Career>

    @Query(
        value = """
        SELECT c.*
          FROM careers c
          JOIN resumes r ON r.id = c.resume_id
         WHERE r.worker_id = :workerId
         ORDER BY c.started_at DESC NULLS LAST, c.ended_at DESC NULLS LAST
        """,
        countQuery = """
        SELECT COUNT(*)
          FROM careers c
          JOIN resumes r ON r.id = c.resume_id
         WHERE r.worker_id = :workerId
        """,
        nativeQuery = true,
    )
    fun findAdminCareersByWorkerIdNative(
        @Param("workerId") workerId: String,
        pageable: Pageable,
    ): Page<Career>
}
