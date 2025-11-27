package com.spectrum.workfolio.services.plan

import com.spectrum.workfolio.domain.entity.plan.Plan
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.PlanType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.PlanRepository
import com.spectrum.workfolio.proto.plan.PlanGetResponse
import com.spectrum.workfolio.proto.plan.PlanListResponse
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Plan 조회 전용 서비스
 */
@Service
@Transactional(readOnly = true)
class PlanQueryService(
    private val planRepository: PlanRepository,
) {

    fun getPlan(id: String): Plan {
        return planRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN.message) }
    }

    fun getPlanByType(type: PlanType): Plan {
        return planRepository.findByType(type)
            ?: throw WorkfolioException(MsgKOR.NOT_FOUND_PLAN.message)
    }

    fun getPlanResult(id: String): PlanGetResponse {
        val plan = getPlan(id)
        return PlanGetResponse.newBuilder()
            .setPlan(plan.toProto())
            .build()
    }

    fun listPlans(): List<Plan> {
        return planRepository.findAllByOrderByPriorityAsc()
    }

    fun listPlansResult(): PlanListResponse {
        val plans = listPlans()
        return PlanListResponse.newBuilder()
            .addAllPlans(plans.map { it.toProto() })
            .build()
    }
}
