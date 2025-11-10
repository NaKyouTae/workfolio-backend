package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.plan.Plan
import com.spectrum.workfolio.domain.enums.PlanType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlanRepository : JpaRepository<Plan, String> {
    fun findByType(type: PlanType): Plan?
    fun findAllByOrderByPriorityAsc(): List<Plan>
}
