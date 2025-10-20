package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Salary
import com.spectrum.workfolio.utils.TimeUtil

fun Salary.toProto(): com.spectrum.workfolio.proto.common.Salary {
    val builder = com.spectrum.workfolio.proto.common.Salary.newBuilder()

    builder.setId(this.id)
    builder.setAmount(this.amount)
    builder.setCareer(this.career.toProto())
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
