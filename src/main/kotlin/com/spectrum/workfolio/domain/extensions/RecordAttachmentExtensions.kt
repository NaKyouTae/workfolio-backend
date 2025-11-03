package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.RecordAttachment
import com.spectrum.workfolio.domain.entity.resume.ResumeAttachment
import com.spectrum.workfolio.utils.TimeUtil

fun RecordAttachment.toProto(): com.spectrum.workfolio.proto.common.RecordAttachment {
    val builder = com.spectrum.workfolio.proto.common.RecordAttachment.newBuilder()

    builder.setId(this.id)
    builder.setFileName(this.fileName)
    builder.setFileUrl(this.fileUrl)
    builder.setRecord(this.record.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun ResumeAttachment.toProtoWithoutRecord(): com.spectrum.workfolio.proto.common.RecordAttachment {
    val builder = com.spectrum.workfolio.proto.common.RecordAttachment.newBuilder()

    builder.setId(this.id)
    builder.setFileName(this.fileName)
    builder.setFileUrl(this.fileUrl)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
