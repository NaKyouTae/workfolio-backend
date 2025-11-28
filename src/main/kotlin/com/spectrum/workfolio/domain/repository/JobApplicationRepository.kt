package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobApplicationRepository : JpaRepository<JobApplication, String> {
    fun findByTurnOverIdOrderByPriorityAsc(turnOverId: String): List<JobApplication>
    fun findByTurnOverIdInOrderByTurnOverIdAscPriorityAsc(turnOverIds: List<String>): List<JobApplication>
}
