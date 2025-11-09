package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.common.Notice
import com.spectrum.workfolio.utils.TimeUtil

fun Notice.toProto(): com.spectrum.workfolio.proto.common.Notice {
    val builder = com.spectrum.workfolio.proto.common.Notice.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setContent(this.content)
    builder.setIsPinned(this.isPinned)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

