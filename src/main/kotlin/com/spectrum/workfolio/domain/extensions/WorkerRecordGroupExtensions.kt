package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import com.spectrum.workfolio.utils.TimeUtil

fun WorkerRecordGroup.toProto(): com.spectrum.workfolio.proto.common.WorkerRecordGroup {
    return com.spectrum.workfolio.proto.common.WorkerRecordGroup.newBuilder()
        .setId(this.id)
        .setPublicId(this.publicId)
        .setRole(com.spectrum.workfolio.proto.common.WorkerRecordGroup.RecordGroupRole.valueOf(this.role.name))
        .setWorker(this.worker.toProto())
        .setRecordGroup(this.recordGroup.toProto())
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
