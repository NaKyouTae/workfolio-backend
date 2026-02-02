package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.payments.CreditPlan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CreditPlanRepository : JpaRepository<CreditPlan, String> {
    fun findByIdAndIsActiveTrue(id: String): CreditPlan?
    fun findAllByIsActiveTrueOrderByDisplayOrderAsc(): List<CreditPlan>
}
