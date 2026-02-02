package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.creditplan.CreditPlanCreateRequest
import com.spectrum.workfolio.proto.creditplan.CreditPlanGetResponse
import com.spectrum.workfolio.proto.creditplan.CreditPlanListResponse
import com.spectrum.workfolio.proto.creditplan.CreditPlanUpdateRequest
import com.spectrum.workfolio.services.CreditPlanService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/credit-plans")
class CreditPlanController(
    private val creditPlanService: CreditPlanService,
) {

    @GetMapping
    fun listActiveCreditPlans(): CreditPlanListResponse {
        val plans = creditPlanService.getActiveCreditPlans()
        val planProtos = plans.map { it.toProto() }
        return CreditPlanListResponse.newBuilder()
            .addAllCreditPlans(planProtos)
            .build()
    }

    @GetMapping("/{id}")
    fun getCreditPlan(
        @PathVariable id: String,
    ): CreditPlanGetResponse {
        val plan = creditPlanService.getActiveCreditPlanById(id)
        return CreditPlanGetResponse.newBuilder()
            .setCreditPlan(plan.toProto())
            .build()
    }

    @PostMapping
    fun createCreditPlan(
        @RequestBody request: CreditPlanCreateRequest,
    ): SuccessResponse {
        creditPlanService.createCreditPlan(
            name = request.name,
            description = if (request.hasDescription()) request.description else null,
            price = request.price,
            baseCredits = request.baseCredits,
            bonusCredits = request.bonusCredits,
            displayOrder = request.displayOrder,
            isPopular = request.isPopular,
        )
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateCreditPlan(
        @RequestBody request: CreditPlanUpdateRequest,
    ): SuccessResponse {
        creditPlanService.updateCreditPlan(
            id = request.id,
            name = request.name,
            description = if (request.hasDescription()) request.description else null,
            price = request.price,
            baseCredits = request.baseCredits,
            bonusCredits = request.bonusCredits,
            displayOrder = request.displayOrder,
            isPopular = request.isPopular,
            isActive = request.isActive,
        )
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deactivateCreditPlan(
        @PathVariable id: String,
    ): SuccessResponse {
        creditPlanService.deactivateCreditPlan(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
