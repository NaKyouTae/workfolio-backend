package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.plan.Plan
import com.spectrum.workfolio.domain.entity.plan.PlanSubscription
import com.spectrum.workfolio.proto.release.ReleasePlanDetail
import com.spectrum.workfolio.proto.release.ReleasePlanSubscription
import com.spectrum.workfolio.utils.TimeUtil

fun PlanSubscription.toReleaseProto(): ReleasePlanSubscription {
    return ReleasePlanSubscription.newBuilder()
        .setId(this.id)
        .setDurationMonths(this.durationMonths.toString())
        .setTotalPrice(this.totalPrice)
        .setMonthlyEquivalent(this.monthlyEquivalent)
        .setSavingsAmount(this.savingsAmount)
        .setDiscountRate(this.discountRate)
        .setPriority(this.priority)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}

fun Plan.toReleaseDetailProto(planSubscriptions: List<PlanSubscription>): ReleasePlanDetail {
    val planSubscriptionProtos = planSubscriptions.map { it.toReleaseProto() }
    
    return ReleasePlanDetail.newBuilder()
        .setId(this.id)
        .setName(this.name)
        .setType(com.spectrum.workfolio.proto.common.Plan.PlanType.valueOf(this.type.name))
        .setPrice(this.price.toLong())
        .setCurrency(this.currency.name)
        .setPriority(this.priority)
        .apply {
            if (this@toReleaseDetailProto.description != null) {
                setDescription(this@toReleaseDetailProto.description)
            }
        }
        .addAllPlanSubscriptions(planSubscriptionProtos)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}

