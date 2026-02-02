package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.payments.CreditPlan
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.CreditPlanRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreditPlanService(
    private val creditPlanRepository: CreditPlanRepository,
) {

    fun getCreditPlanById(id: String): CreditPlan {
        return creditPlanRepository.findById(id)
            .orElseThrow { WorkfolioException("크레딧 플랜을 찾을 수 없습니다.") }
    }

    fun getActiveCreditPlanById(id: String): CreditPlan {
        return creditPlanRepository.findByIdAndIsActiveTrue(id)
            ?: throw WorkfolioException("크레딧 플랜을 찾을 수 없습니다.")
    }

    @Transactional(readOnly = true)
    fun getActiveCreditPlans(): List<CreditPlan> {
        return creditPlanRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
    }

    @Transactional
    fun createCreditPlan(
        name: String,
        description: String?,
        price: Int,
        baseCredits: Int,
        bonusCredits: Int,
        displayOrder: Int,
        isPopular: Boolean,
    ): CreditPlan {
        val creditPlan = CreditPlan(
            name = name,
            description = description,
            price = price,
            baseCredits = baseCredits,
            bonusCredits = bonusCredits,
            displayOrder = displayOrder,
            isPopular = isPopular,
        )
        return creditPlanRepository.save(creditPlan)
    }

    @Transactional
    fun updateCreditPlan(
        id: String,
        name: String,
        description: String?,
        price: Int,
        baseCredits: Int,
        bonusCredits: Int,
        displayOrder: Int,
        isPopular: Boolean,
        isActive: Boolean,
    ): CreditPlan {
        val creditPlan = getCreditPlanById(id)
        creditPlan.changeInfo(
            name = name,
            description = description,
            price = price,
            baseCredits = baseCredits,
            bonusCredits = bonusCredits,
            isActive = isActive,
            displayOrder = displayOrder,
            isPopular = isPopular,
        )
        return creditPlanRepository.save(creditPlan)
    }

    @Transactional
    fun deactivateCreditPlan(id: String) {
        val creditPlan = getCreditPlanById(id)
        creditPlan.deactivate()
        creditPlanRepository.save(creditPlan)
    }
}
