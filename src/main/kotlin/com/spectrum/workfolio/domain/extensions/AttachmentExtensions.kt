package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Achievement
import com.spectrum.workfolio.domain.entity.resume.Attachment
import com.spectrum.workfolio.utils.TimeUtil

fun Attachment.toProto(): com.spectrum.workfolio.proto.common.Attachment {
    val builder = com.spectrum.workfolio.proto.common.Attachment.newBuilder()

    builder.setId(this.id)
    builder.setFileName(this.fileName)
    builder.setFileUrl(this.fileUrl)

    builder.setResume(this.resume.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }

    return builder.build()
}

fun Attachment.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.Attachment {
    val builder = com.spectrum.workfolio.proto.common.Attachment.newBuilder()

    builder.setId(this.id)
    builder.setFileName(this.fileName)
    builder.setFileUrl(this.fileUrl)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }

    return builder.build()
}
