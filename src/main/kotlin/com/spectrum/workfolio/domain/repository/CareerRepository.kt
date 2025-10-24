package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Career
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CareerRepository : JpaRepository<Career, String> {
    fun findByResumeIdOrderByPriorityAscStartedAtDescEndedAtDesc(resumeId: String): List<Career>
}
