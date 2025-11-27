package com.spectrum.workfolio.services.plan

import com.spectrum.workfolio.domain.entity.plan.PlanFeature
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.PlanFeatureRepository
import com.spectrum.workfolio.proto.planfeature.PlanFeatureCreateRequest
import com.spectrum.workfolio.proto.planfeature.PlanFeatureUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * PlanFeature 명령 전용 서비스
 */
@Service
@Transactional
class PlanFeatureCommandService(
    private val planFeatureRepository: PlanFeatureRepository,
    private val planQueryService: PlanQueryService,
    private val featureQueryService: FeatureQueryService,
) {

    fun createPlanFeature(request: PlanFeatureCreateRequest): PlanFeature {
        val plan = planQueryService.getPlan(request.planId)
        val feature = featureQueryService.getFeature(request.featureId)

        // 중복 체크
        planFeatureRepository.findByPlanIdAndFeatureId(plan.id, feature.id)
            ?.let { throw WorkfolioException(MsgKOR.ALREADY_EXISTS_PLAN_FEATURE.message) }

        val planFeature = PlanFeature(
            plan = plan,
            feature = feature,
            limitCount = request.limitCount,
            description = request.description,
        )
        return planFeatureRepository.save(planFeature)
    }

    fun updatePlanFeature(request: PlanFeatureUpdateRequest): PlanFeature {
        val planFeature = planFeatureRepository.findById(request.id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN_FEATURE.message) }

        val plan = planQueryService.getPlan(request.planId)
        val feature = featureQueryService.getFeature(request.featureId)

        planFeature.changeInfo(
            plan = plan,
            feature = feature,
            limitCount = request.limitCount,
            description = request.description,
        )

        return planFeatureRepository.save(planFeature)
    }

    fun deletePlanFeature(id: String) {
        val planFeature = planFeatureRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN_FEATURE.message) }

        planFeatureRepository.delete(planFeature)
    }
}
