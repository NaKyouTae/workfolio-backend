package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.utils.TimeUtil

fun Record.toProto(): com.spectrum.workfolio.proto.common.Record {
    val builder = com.spectrum.workfolio.proto.common.Record.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setType(com.spectrum.workfolio.proto.common.Record.RecordType.valueOf(this.type.name))
    builder.setDescription(this.description)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setRecordGroup(this.recordGroup.toProto())
    builder.setWorker(this.worker.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) { builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!)) }
    if (this.company != null) { builder.setCompany(this.company!!.toProto()) }

    return builder.build()
}
