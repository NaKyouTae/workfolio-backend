package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionCreateRequest
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionGetResponse
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionListResponse
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionUpdateRequest
import com.spectrum.workfolio.services.PlanSubscriptionService
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
@RequestMapping("/api/plan-subscriptions")
class PlanSubscriptionController(
    private val planSubscriptionService: PlanSubscriptionService,
) {

    @GetMapping
    fun getPlanSubscriptions(
        @RequestParam(required = false) planId: String?,
    ): PlanSubscriptionListResponse {
        return if (planId != null) {
            planSubscriptionService.getPlanSubscriptionsByPlanId(planId)
        } else {
            planSubscriptionService.getPlanSubscriptions()
        }
    }

    @GetMapping("/{id}")
    fun getPlanSubscription(
        @PathVariable id: String,
    ): PlanSubscriptionGetResponse {
        return planSubscriptionService.getPlanSubscription(id)
    }

    @PostMapping
    fun createPlanSubscription(
        @RequestBody request: PlanSubscriptionCreateRequest,
    ): SuccessResponse {
        planSubscriptionService.createPlanSubscription(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updatePlanSubscription(
        @RequestBody request: PlanSubscriptionUpdateRequest,
    ): SuccessResponse {
        planSubscriptionService.updatePlanSubscription(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deletePlanSubscription(
        @PathVariable id: String,
    ): SuccessResponse {
        planSubscriptionService.deletePlanSubscription(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
