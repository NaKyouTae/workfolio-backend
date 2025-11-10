package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureCreateRequest
import com.spectrum.workfolio.proto.planfeature.PlanFeatureGetResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureListResponse
import com.spectrum.workfolio.proto.planfeature.PlanFeatureUpdateRequest
import com.spectrum.workfolio.services.PlanFeatureService
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
    private val planFeatureService: PlanFeatureService,
) {

    @GetMapping
    fun getPlanFeatures(
        @RequestParam(required = false) planId: String?,
    ): PlanFeatureListResponse {
        return if (planId != null) {
            planFeatureService.getPlanFeaturesByPlanId(planId)
        } else {
            planFeatureService.getPlanFeatures()
        }
    }

    @GetMapping("/{id}")
    fun getPlanFeature(
        @PathVariable id: String,
    ): PlanFeatureGetResponse {
        return planFeatureService.getPlanFeature(id)
    }

    @PostMapping
    fun createPlanFeature(
        @RequestBody request: PlanFeatureCreateRequest,
    ): SuccessResponse {
        planFeatureService.createPlanFeature(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updatePlanFeature(
        @RequestBody request: PlanFeatureUpdateRequest,
    ): SuccessResponse {
        planFeatureService.updatePlanFeature(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deletePlanFeature(
        @PathVariable id: String,
    ): SuccessResponse {
        planFeatureService.deletePlanFeature(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
