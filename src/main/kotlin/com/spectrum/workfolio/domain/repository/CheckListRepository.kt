package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.CheckList
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CheckListRepository : JpaRepository<CheckList, String> {
    fun findByTurnOverGoalIdOrderByPriorityAsc(turnOverGoalId: String): List<CheckList>
}
