package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.plan.PlanFeature
import com.spectrum.workfolio.utils.TimeUtil

fun PlanFeature.toProto(): com.spectrum.workfolio.proto.common.PlanFeature {
    val builder = com.spectrum.workfolio.proto.common.PlanFeature.newBuilder()

    builder.setId(this.id)
    builder.setPlan(this.plan.toProto())
    builder.setFeature(this.feature.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
