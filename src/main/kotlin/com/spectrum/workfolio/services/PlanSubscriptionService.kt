package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.plan.PlanSubscription
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.PlanSubscriptionRepository
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionCreateRequest
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionGetResponse
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionListResponse
import com.spectrum.workfolio.proto.plansubscription.PlanSubscriptionUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlanSubscriptionService(
    private val planSubscriptionRepository: PlanSubscriptionRepository,
    private val planService: PlanService,
) {

    fun getPlanSubscriptionById(id: String): PlanSubscription {
        return planSubscriptionRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PLAN_SUBSCRIPTION.message) }
    }

    @Transactional(readOnly = true)
    fun getPlanSubscriptions(): PlanSubscriptionListResponse {
        val planSubscriptions = planSubscriptionRepository.findAllByOrderByPriorityAsc()
        val planSubscriptionProtos = planSubscriptions.map { it.toProto() }
        return PlanSubscriptionListResponse.newBuilder().addAllPlanSubscriptions(planSubscriptionProtos).build()
    }

    @Transactional(readOnly = true)
    fun getPlanSubscriptionsByPlanId(planId: String): PlanSubscriptionListResponse {
        val planSubscriptions = planSubscriptionRepository.findByPlanIdOrderByPriorityAsc(planId)
        val planSubscriptionProtos = planSubscriptions.map { it.toProto() }
        return PlanSubscriptionListResponse.newBuilder().addAllPlanSubscriptions(planSubscriptionProtos).build()
    }

    @Transactional(readOnly = true)
    fun getPlanSubscription(id: String): PlanSubscriptionGetResponse {
        val planSubscription = getPlanSubscriptionById(id)
        return PlanSubscriptionGetResponse.newBuilder().setPlanSubscription(planSubscription.toProto()).build()
    }

    @Transactional
    fun createPlanSubscription(request: PlanSubscriptionCreateRequest) {
        val plan = planService.getPlanById(request.planId)

        val planSubscription = PlanSubscription(
            durationMonths = request.durationMonths.toInt(),
            totalPrice = request.totalPrice,
            monthlyEquivalent = request.monthlyEquivalent,
            savingsAmount = request.savingsAmount,
            discountRate = request.discountRate,
            priority = request.priority,
            plan = plan,
        )

        planSubscriptionRepository.save(planSubscription)
    }

    @Transactional
    fun updatePlanSubscription(request: PlanSubscriptionUpdateRequest) {
        val planSubscription = getPlanSubscriptionById(request.id)
        val plan = planService.getPlanById(request.planId)

        planSubscription.changeInfo(
            durationMonths = request.durationMonths.toInt(),
            totalPrice = request.totalPrice,
            monthlyEquivalent = request.monthlyEquivalent,
            savingsAmount = request.savingsAmount,
            discountRate = request.discountRate,
            priority = request.priority,
            plan = plan,
        )

        planSubscriptionRepository.save(planSubscription)
    }

    @Transactional
    fun deletePlanSubscription(id: String) {
        val planSubscription = getPlanSubscriptionById(id)
        planSubscriptionRepository.delete(planSubscription)
    }
}

