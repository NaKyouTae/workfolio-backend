package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.utils.TimeUtil

fun Worker.toWorkerProto(): com.spectrum.workfolio.proto.common.Worker {
    return com.spectrum.workfolio.proto.common.Worker.newBuilder()
        .setId(this.id)
        .setName(this.name)
        .setNickName(this.nickName)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
