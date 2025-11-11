package com.spectrum.workfolio.services.plan

import com.spectrum.workfolio.domain.entity.plan.PlanFeature
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.PlanFeatureRepository
import com.spectrum.workfolio.proto.planfeature.PlanFeatureGetResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureListResponse
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * PlanFeature 조회 전용 서비스
 */
@Service
@Transactional(readOnly = true)
class PlanFeatureQueryService(
    private val planFeatureRepository: PlanFeatureRepository,
) {

    fun getPlanFeature(id: String): PlanFeature {
        return planFeatureRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN_FEATURE.message) }
    }

    fun getPlanFeatureResult(id: String): PlanFeatureGetResponse {
        val planFeature = getPlanFeature(id)
        return PlanFeatureGetResponse.newBuilder()
            .setPlanFeature(planFeature.toProto())
            .build()
    }

    fun listPlanFeatures(): List<PlanFeature> {
        return planFeatureRepository.findAll()
    }

    fun listPlanFeaturesByPlanId(planId: String): List<PlanFeature> {
        return planFeatureRepository.findByPlanId(planId)
    }

    fun listPlanFeaturesByFeatureId(featureId: String): List<PlanFeature> {
        return planFeatureRepository.findByFeatureId(featureId)
    }

    fun listPlanFeaturesResult(): PlanFeatureListResponse {
        val planFeatures = listPlanFeatures()
        return PlanFeatureListResponse.newBuilder()
            .addAllPlanFeatures(planFeatures.map { it.toProto() })
            .build()
    }

    fun listPlanFeaturesByPlanIdResult(planId: String): PlanFeatureListResponse {
        val planFeatures = listPlanFeaturesByPlanId(planId)
        return PlanFeatureListResponse.newBuilder()
            .addAllPlanFeatures(planFeatures.map { it.toProto() })
            .build()
    }

    fun listPlanFeaturesByFeatureIdResult(featureId: String): PlanFeatureListResponse {
        val planFeatures = listPlanFeaturesByFeatureId(featureId)
        return PlanFeatureListResponse.newBuilder()
            .addAllPlanFeatures(planFeatures.map { it.toProto() })
            .build()
    }
}

