package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Attachment
import com.spectrum.workfolio.utils.TimeUtil

fun Attachment.toProto(): com.spectrum.workfolio.proto.common.Attachment {
    val builder = com.spectrum.workfolio.proto.common.Attachment.newBuilder()

    builder.setId(this.id)
    builder.setFileName(this.fileName)
    builder.setFileUrl(this.fileUrl)
    builder.setUrl(this.url)
    builder.setCategory(
        com.spectrum.workfolio.proto.common.Attachment.AttachmentCategory.valueOf(this.category.name),
    )
    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)
    builder.setTargetId(this.targetId)
    builder.setTargetType(
        com.spectrum.workfolio.proto.common.Attachment.AttachmentTargetType.valueOf(this.targetType.name),
    )

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.type != null) {
        builder.setType(
            com.spectrum.workfolio.proto.common.Attachment.AttachmentType.valueOf(this.type!!.name),
        )
    }

    return builder.build()
}
