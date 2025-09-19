package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.history.Position
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PositionRepository : JpaRepository<Position, String> {
    fun findByCompanyIdInOrderByStartedAtDescEndedAtDesc(companyIds: List<String>): List<Position>
}
