package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.proto.record.CreateRecordResponse
import com.spectrum.workfolio.utils.TimeUtil

fun Record.toProtoResponse(): CreateRecordResponse {
    return CreateRecordResponse.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setDescription(this.description)
        .setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .apply {
            this@toProtoResponse.endedAt?.let { setEndedAt(TimeUtil.toEpochMilli(it)) }
        }
        .build()
}

fun Record.toRecordProto(): com.spectrum.workfolio.proto.common.Record {
    val type = com.spectrum.workfolio.proto.common.Record.RecordType.valueOf(this.type.name)
    val recordGroup = this.recordGroup.toRecordGroupProto()
    val worker = this.worker.toWorkerProto()
    return com.spectrum.workfolio.proto.common.Record.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setType(type)
        .setDescription(this.description)
        .setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
        .apply {
            this@toRecordProto.endedAt?.let { setEndedAt(TimeUtil.toEpochMilli(it)) }
        }
        .setRecordGroup(recordGroup)
        .setWorker(worker)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
