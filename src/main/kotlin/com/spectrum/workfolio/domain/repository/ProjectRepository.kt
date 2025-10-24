package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : JpaRepository<Project, String> {

    fun findByResumeId(resumeId: String): List<Project>

    @Query("SELECT p FROM Project p WHERE p.resume.worker.id = :workerId")
    fun findByWorkerId(@Param("workerId") workerId: String): List<Project>

    @Query("SELECT p FROM Project p WHERE p.resume.id = :resumeId AND p.isVisible = true")
    fun findVisibleByResumeId(@Param("resumeId") resumeId: String): List<Project>
}
