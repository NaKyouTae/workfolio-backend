package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.plan.PlanFeature
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.PlanFeatureRepository
import com.spectrum.workfolio.proto.planfeature.PlanFeatureCreateRequest
import com.spectrum.workfolio.proto.planfeature.PlanFeatureGetResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureListResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlanFeatureService(
    private val planFeatureRepository: PlanFeatureRepository,
    private val planService: PlanService,
    private val featureService: FeatureService,
) {

    fun getPlanFeatureById(id: String): PlanFeature {
        return planFeatureRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN_FEATURE.message) }
    }

    @Transactional(readOnly = true)
    fun getPlanFeatures(): PlanFeatureListResponse {
        val planFeatures = planFeatureRepository.findAll()
        val planFeatureProtos = planFeatures.map { it.toProto() }
        return PlanFeatureListResponse.newBuilder().addAllPlanFeatures(planFeatureProtos).build()
    }

    @Transactional(readOnly = true)
    fun getPlanFeaturesByPlanId(planId: String): PlanFeatureListResponse {
        val planFeatures = planFeatureRepository.findByPlanId(planId)
        val planFeatureProtos = planFeatures.map { it.toProto() }
        return PlanFeatureListResponse.newBuilder().addAllPlanFeatures(planFeatureProtos).build()
    }

    @Transactional(readOnly = true)
    fun getPlanFeature(id: String): PlanFeatureGetResponse {
        val planFeature = getPlanFeatureById(id)
        return PlanFeatureGetResponse.newBuilder().setPlanFeature(planFeature.toProto()).build()
    }

    @Transactional
    fun createPlanFeature(request: PlanFeatureCreateRequest) {
        // 중복 체크
        val existing = planFeatureRepository.findByPlanIdAndFeatureId(request.planId, request.featureId)
        if (existing != null) {
            throw WorkfolioException("이미 존재하는 플랜-기능 조합입니다.")
        }

        val plan = planService.getPlanById(request.planId)
        val feature = featureService.getFeatureById(request.featureId)

        val planFeature = PlanFeature(
            plan = plan,
            feature = feature,
            limitCount = request.limitCount.takeIf { it > 0 },
            description = request.description,
        )

        planFeatureRepository.save(planFeature)
    }

    @Transactional
    fun updatePlanFeature(request: PlanFeatureUpdateRequest) {
        val planFeature = getPlanFeatureById(request.id)

        // 중복 체크 (자기 자신 제외)
        val existing = planFeatureRepository.findByPlanIdAndFeatureId(request.planId, request.featureId)
        if (existing != null && existing.id != request.id) {
            throw WorkfolioException("이미 존재하는 플랜-기능 조합입니다.")
        }

        val plan = planService.getPlanById(request.planId)
        val feature = featureService.getFeatureById(request.featureId)
        val limitCount = request.limitCount.takeIf { it > 0 }

        planFeature.changeInfo(
            plan = plan,
            feature = feature,
            limitCount = limitCount,
            description = request.description,
        )

        planFeatureRepository.save(planFeature)
    }

    @Transactional
    fun deletePlanFeature(id: String) {
        val planFeature = getPlanFeatureById(id)
        planFeatureRepository.delete(planFeature)
    }
}
