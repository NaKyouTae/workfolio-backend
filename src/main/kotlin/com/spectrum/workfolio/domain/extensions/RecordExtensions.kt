package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.utils.TimeUtil

fun Record.toProto(): com.spectrum.workfolio.proto.common.Record {
    return com.spectrum.workfolio.proto.common.Record.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setType(com.spectrum.workfolio.proto.common.Record.RecordType.valueOf(this.type.name))
        .setDescription(this.description)
        .setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
        .apply {
            this@toProto.endedAt?.let { setEndedAt(TimeUtil.toEpochMilli(it)) }
        }
        .setRecordGroup(this.recordGroup.toProto())
        .setWorker(this.worker.toProto())
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
