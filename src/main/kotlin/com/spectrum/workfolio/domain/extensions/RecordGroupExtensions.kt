package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.utils.TimeUtil

fun RecordGroup.toProto(): com.spectrum.workfolio.proto.common.RecordGroup {
    return com.spectrum.workfolio.proto.common.RecordGroup.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setIsPublic(this.isPublic)
        .setPublicId(this.publicId)
        .setColor(this.color)
        .setPriority(this.priority)
        .setWorker(this.worker.toProto())
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
