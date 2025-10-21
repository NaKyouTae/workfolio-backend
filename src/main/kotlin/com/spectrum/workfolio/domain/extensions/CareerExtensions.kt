package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Career
import com.spectrum.workfolio.utils.TimeUtil

fun Career.toProto(): com.spectrum.workfolio.proto.common.Career {
    val builder = com.spectrum.workfolio.proto.common.Career.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setIsWorking(this.isWorking)
    builder.setResume(this.resume.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}

fun Career.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.Career {
    val builder = com.spectrum.workfolio.proto.common.Career.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setIsWorking(this.isWorking)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
