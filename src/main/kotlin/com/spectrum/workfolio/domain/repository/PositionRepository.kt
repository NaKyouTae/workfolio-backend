package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.history.Position
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PositionRepository : JpaRepository<Position, String> {
    @Query("SELECT p FROM Position p WHERE p.company.id IN :companyIds")
    fun findPositionsByCompanyIds(companyIds: List<String>): List<Position>
}
