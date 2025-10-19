package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Degrees
import com.spectrum.workfolio.utils.TimeUtil

fun Degrees.toProto(): com.spectrum.workfolio.proto.common.Degrees {
    val builder = com.spectrum.workfolio.proto.common.Degrees.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setMajor(this.major)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setResume(this.resume.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
