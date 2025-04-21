package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.proto.CreateRecordGroupResponse
import com.spectrum.workfolio.utils.TimeUtil

fun RecordGroup.toProtoResponse(): CreateRecordGroupResponse {
    return CreateRecordGroupResponse.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setIsPublic(this.isPublic)
        .setPublicId(this.publicId)
        .setColor(this.color)
        .setPriority(this.priority)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}

fun RecordGroup.toRecordGroupProto(): com.spectrum.workfolio.proto.RecordGroup {
    return com.spectrum.workfolio.proto.RecordGroup.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setIsPublic(this.isPublic)
        .setPublicId(this.publicId)
        .setColor(this.color)
        .setPriority(this.priority)
        .setWorker(this.worker.toWorkerProto())
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
