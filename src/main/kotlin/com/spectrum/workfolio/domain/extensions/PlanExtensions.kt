package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.plan.Plan
import com.spectrum.workfolio.utils.TimeUtil

fun Plan.toProto(): com.spectrum.workfolio.proto.common.Plan {
    val builder = com.spectrum.workfolio.proto.common.Plan.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setType(com.spectrum.workfolio.proto.common.Plan.PlanType.valueOf(this.type.name))
    builder.setPrice(this.price.toLong())
    builder.setCurrency(this.currency.name)
    builder.setPriority(this.priority)
    if (this.description != null) {
        builder.setDescription(this.description)
    }

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
