package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.SelfIntroduction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SelfIntroductionRepository : JpaRepository<SelfIntroduction, String> {
    fun findByTurnOverGoalIdOrderByPriorityAsc(turnOverGoalId: String): List<SelfIntroduction>
}
