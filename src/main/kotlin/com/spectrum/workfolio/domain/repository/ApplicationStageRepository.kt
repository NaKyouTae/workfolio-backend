package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.ApplicationStage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationStageRepository : JpaRepository<ApplicationStage, String> {
    fun findByJobApplicationId(jobApplicationId: String): List<ApplicationStage>
}
