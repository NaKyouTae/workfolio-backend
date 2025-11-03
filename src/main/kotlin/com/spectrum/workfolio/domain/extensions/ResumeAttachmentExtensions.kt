package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.ResumeAttachment
import com.spectrum.workfolio.utils.TimeUtil

fun ResumeAttachment.toProto(): com.spectrum.workfolio.proto.common.ResumeAttachment {
    val builder = com.spectrum.workfolio.proto.common.ResumeAttachment.newBuilder()

    builder.setId(this.id)
    builder.setFileName(this.fileName)
    builder.setFileUrl(this.fileUrl)
    builder.setUrl(this.url)
    builder.setCategory(
        com.spectrum.workfolio.proto.common.ResumeAttachment.ResumeAttachmentCategory.valueOf(this.category.name),
    )
    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)
    builder.setResume(this.resume.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.type != null) {
        builder.setType(
            com.spectrum.workfolio.proto.common.ResumeAttachment.ResumeAttachmentType.valueOf(this.type!!.name),
        )
    }

    return builder.build()
}

fun ResumeAttachment.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.ResumeAttachment {
    val builder = com.spectrum.workfolio.proto.common.ResumeAttachment.newBuilder()

    builder.setId(this.id)
    builder.setFileName(this.fileName)
    builder.setFileUrl(this.fileUrl)
    builder.setUrl(this.url)
    builder.setCategory(
        com.spectrum.workfolio.proto.common.ResumeAttachment.ResumeAttachmentCategory.valueOf(this.category.name),
    )
    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.type != null) {
        builder.setType(
            com.spectrum.workfolio.proto.common.ResumeAttachment.ResumeAttachmentType.valueOf(this.type!!.name),
        )
    }

    return builder.build()
}
