package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TurnOverRepository : JpaRepository<TurnOver, Long> {
    fun findByWorkerId(workerId: String): List<TurnOver>
}
