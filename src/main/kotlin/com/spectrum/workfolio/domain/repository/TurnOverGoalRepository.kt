package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TurnOverGoalRepository : JpaRepository<TurnOverGoal, String>
