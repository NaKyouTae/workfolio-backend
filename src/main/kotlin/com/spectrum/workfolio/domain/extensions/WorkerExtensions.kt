package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.utils.TimeUtil

fun Worker.toWorkerProto(): com.spectrum.workfolio.proto.Worker {
    return com.spectrum.workfolio.proto.Worker.newBuilder()
        .setId(this.id)
        .setName(this.name)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
