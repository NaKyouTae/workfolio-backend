package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.SystemConfig
import com.spectrum.workfolio.utils.TimeUtil

fun SystemConfig.toProto(): com.spectrum.workfolio.proto.common.SystemConfig {
    return com.spectrum.workfolio.proto.common.SystemConfig.newBuilder()
        .setId(this.id)
        .setType(com.spectrum.workfolio.proto.common.SystemConfig.SystemConfigType.valueOf(this.type.name))
        .setValue(this.value)
        .setWorker(this.worker.toProto())
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
