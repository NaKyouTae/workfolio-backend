package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.plan.PlanSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlanSubscriptionRepository : JpaRepository<PlanSubscription, String> {
    fun findByPlanIdOrderByPriorityAsc(planId: String): List<PlanSubscription>
    fun findAllByOrderByPriorityAsc(): List<PlanSubscription>
}
