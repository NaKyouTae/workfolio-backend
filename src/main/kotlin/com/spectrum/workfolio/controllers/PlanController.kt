package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.plan.PlanCreateRequest
import com.spectrum.workfolio.proto.plan.PlanGetResponse
import com.spectrum.workfolio.proto.plan.PlanListResponse
import com.spectrum.workfolio.proto.plan.PlanUpdateRequest
import com.spectrum.workfolio.services.plan.PlanCommandService
import com.spectrum.workfolio.services.plan.PlanQueryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/plans")
class PlanController(
    private val planQueryService: PlanQueryService,
    private val planCommandService: PlanCommandService,
) {

    @GetMapping
    fun listPlans(): PlanListResponse {
        return planQueryService.listPlansResult()
    }

    @GetMapping("/{id}")
    fun getPlan(
        @PathVariable id: String,
    ): PlanGetResponse {
        return planQueryService.getPlanResult(id)
    }

    @PostMapping
    fun createPlan(
        @RequestBody request: PlanCreateRequest,
    ): SuccessResponse {
        planCommandService.createPlan(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updatePlan(
        @RequestBody request: PlanUpdateRequest,
    ): SuccessResponse {
        planCommandService.updatePlan(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deletePlan(
        @PathVariable id: String,
    ): SuccessResponse {
        planCommandService.deletePlan(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
