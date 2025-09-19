package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.history.Company
import com.spectrum.workfolio.utils.TimeUtil

fun Company.toProto(): com.spectrum.workfolio.proto.common.Company {
    val builder = com.spectrum.workfolio.proto.common.Company.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setIsWorking(this.isWorking)
    builder.setWorker(this.worker.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
