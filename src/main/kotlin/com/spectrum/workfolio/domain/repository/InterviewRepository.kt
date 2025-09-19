package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.interview.Interview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewRepository : JpaRepository<Interview, String> {
    fun findByJobSearchCompanyIdOrderByStartedAtDescEndedAtDesc(workerId: String): List<Interview>
}
