package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.plan.Feature
import com.spectrum.workfolio.utils.TimeUtil

fun Feature.toProto(): com.spectrum.workfolio.proto.common.Feature {
    val builder = com.spectrum.workfolio.proto.common.Feature.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setDomain(this.domain)
    builder.setAction(this.action)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
