package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Position
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PositionRepository : JpaRepository<Position, String> {
    fun findByCareerIdInOrderByStartedAtDescEndedAtDesc(careerIds: List<String>): List<Position>
}
