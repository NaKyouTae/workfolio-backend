package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.InterviewQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewQuestionRepository : JpaRepository<InterviewQuestion, String> {
    fun findByTurnOverGoalId(turnOverGoalId: String): List<InterviewQuestion>
}
