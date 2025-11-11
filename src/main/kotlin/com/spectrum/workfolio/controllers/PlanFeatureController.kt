package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureCreateRequest
import com.spectrum.workfolio.proto.planfeature.PlanFeatureGetResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureListResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureUpdateRequest
import com.spectrum.workfolio.services.plan.PlanFeatureCommandService
import com.spectrum.workfolio.services.plan.PlanFeatureQueryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/plan-features")
class PlanFeatureController(
    private val planFeatureQueryService: PlanFeatureQueryService,
    private val planFeatureCommandService: PlanFeatureCommandService,
) {

    @GetMapping
    fun listPlanFeatures(
        @RequestParam(name = "plan_id", required = false) planId: String?,
        @RequestParam(name = "feature_id", required = false) featureId: String?,
    ): PlanFeatureListResponse {
        return when {
            planId != null -> planFeatureQueryService.listPlanFeaturesByPlanIdResult(planId)
            featureId != null -> planFeatureQueryService.listPlanFeaturesByFeatureIdResult(featureId)
            else -> planFeatureQueryService.listPlanFeaturesResult()
        }
    }

    @GetMapping("/{id}")
    fun getPlanFeature(
        @PathVariable id: String,
    ): PlanFeatureGetResponse {
        return planFeatureQueryService.getPlanFeatureResult(id)
    }

    @PostMapping
    fun createPlanFeature(
        @RequestBody request: PlanFeatureCreateRequest,
    ): SuccessResponse {
        planFeatureCommandService.createPlanFeature(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updatePlanFeature(
        @RequestBody request: PlanFeatureUpdateRequest,
    ): SuccessResponse {
        planFeatureCommandService.updatePlanFeature(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deletePlanFeature(
        @PathVariable id: String,
    ): SuccessResponse {
        planFeatureCommandService.deletePlanFeature(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
