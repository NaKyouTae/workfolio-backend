package com.spectrum.workfolio.services.plan

import com.spectrum.workfolio.domain.entity.plan.Plan
import com.spectrum.workfolio.domain.enums.Currency
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.PlanType
import com.spectrum.workfolio.domain.repository.PlanRepository
import com.spectrum.workfolio.proto.plan.PlanCreateRequest
import com.spectrum.workfolio.proto.plan.PlanUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Plan 명령 전용 서비스
 */
@Service
@Transactional
class PlanCommandService(
    private val planRepository: PlanRepository,
) {

    fun createPlan(request: PlanCreateRequest): Plan {
        val plan = Plan(
            name = request.name,
            type = PlanType.valueOf(request.type.name),
            price = BigDecimal.valueOf(request.price.toLong()),
            currency = Currency.valueOf(request.currency),
            priority = request.priority,
            description = if (request.hasDescription()) request.description else null,
        )
        return planRepository.save(plan)
    }

    fun updatePlan(request: PlanUpdateRequest): Plan {
        val plan = planRepository.findById(request.id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN.message) }

        plan.changeInfo(
            name = request.name,
            price = BigDecimal.valueOf(request.price.toLong()),
            currency = Currency.valueOf(request.currency),
            priority = request.priority,
            description = if (request.hasDescription()) request.description else null,
        )

        return planRepository.save(plan)
    }

    fun deletePlan(id: String) {
        val plan = planRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN.message) }

        planRepository.delete(plan)
    }
}

