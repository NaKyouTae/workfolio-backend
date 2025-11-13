package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.plan.PlanSubscription
import com.spectrum.workfolio.utils.TimeUtil

fun PlanSubscription.toProto(): com.spectrum.workfolio.proto.common.PlanSubscription {
    val builder = com.spectrum.workfolio.proto.common.PlanSubscription.newBuilder()

    builder.setId(this.id)
    builder.setDurationMonths(this.durationMonths.toString())
    builder.setTotalPrice(this.totalPrice)
    builder.setMonthlyEquivalent(this.monthlyEquivalent)
    builder.setSavingsAmount(this.savingsAmount)
    builder.setDiscountRate(this.discountRate)
    builder.setPriority(this.priority)
    builder.setPlan(this.plan.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

