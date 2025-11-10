package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.plan.Plan
import com.spectrum.workfolio.domain.enums.Currency
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.PlanType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.PlanRepository
import com.spectrum.workfolio.proto.plan.PlanCreateRequest
import com.spectrum.workfolio.proto.plan.PlanGetResponse
import com.spectrum.workfolio.proto.plan.PlanListResponse
import com.spectrum.workfolio.proto.plan.PlanUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class PlanService(
    private val planRepository: PlanRepository,
) {

    fun getPlanById(id: String): Plan {
        return planRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN.message) }
    }

    fun getPlanByType(type: PlanType): Plan {
        return planRepository.findByType(type)
            ?: throw WorkfolioException(MsgKOR.NOT_FOUND_PLAN.message)
    }

    @Transactional(readOnly = true)
    fun getPlans(): PlanListResponse {
        val plans = planRepository.findAllByOrderByPriorityAsc()
        val planProtos = plans.map { it.toProto() }
        return PlanListResponse.newBuilder().addAllPlans(planProtos).build()
    }

    @Transactional(readOnly = true)
    fun getPlan(id: String): PlanGetResponse {
        val plan = getPlanById(id)
        return PlanGetResponse.newBuilder().setPlan(plan.toProto()).build()
    }

    @Transactional
    fun createPlan(request: PlanCreateRequest) {
        val currency = Currency.valueOf(request.currency)
        val planType = PlanType.valueOf(request.type.name)

        val plan = Plan(
            name = request.name,
            type = planType,
            price = BigDecimal(request.price),
            currency = currency,
            priority = request.priority,
            description = request.description.takeIf { it.isNotBlank() },
        )

        planRepository.save(plan)
    }

    @Transactional
    fun updatePlan(request: PlanUpdateRequest) {
        val plan = getPlanById(request.id)
        val currency = Currency.valueOf(request.currency)

        plan.changeInfo(
            name = request.name,
            price = BigDecimal(request.price),
            currency = currency,
            priority = request.priority,
            description = request.description.takeIf { it.isNotBlank() },
        )

        planRepository.save(plan)
    }

    @Transactional
    fun deletePlan(id: String) {
        val plan = getPlanById(id)
        planRepository.delete(plan)
    }
}
