package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.plan.PlanFeature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlanFeatureRepository : JpaRepository<PlanFeature, String> {
    fun findByPlanId(planId: String): List<PlanFeature>
    fun findByFeatureId(featureId: String): List<PlanFeature>
    fun findByPlanIdAndFeatureId(planId: String, featureId: String): PlanFeature?
}
