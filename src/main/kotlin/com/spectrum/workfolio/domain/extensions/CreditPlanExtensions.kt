package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.payments.CreditPlan
import com.spectrum.workfolio.utils.TimeUtil

fun CreditPlan.toProto(): com.spectrum.workfolio.proto.common.CreditPlan {
    val builder = com.spectrum.workfolio.proto.common.CreditPlan.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    if (this.description != null) {
        builder.setDescription(this.description)
    }
    builder.setPrice(this.price)
    builder.setBaseCredits(this.baseCredits)
    builder.setBonusCredits(this.bonusCredits)
    builder.setTotalCredits(this.totalCredits)
    builder.setIsActive(this.isActive)
    builder.setDisplayOrder(this.displayOrder)
    builder.setIsPopular(this.isPopular)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
