package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.history.Position
import com.spectrum.workfolio.utils.TimeUtil

fun Position.toProto(): com.spectrum.workfolio.proto.common.Position {
    val builder = com.spectrum.workfolio.proto.common.Position.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
