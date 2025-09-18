package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.primary.Education
import com.spectrum.workfolio.utils.TimeUtil

fun Education.toProto(): com.spectrum.workfolio.proto.common.Education {
    val builder = com.spectrum.workfolio.proto.common.Education.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setAgency(this.agency)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setWorker(this.worker.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
