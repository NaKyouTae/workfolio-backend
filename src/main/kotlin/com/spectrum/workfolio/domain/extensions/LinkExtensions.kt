package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Link
import com.spectrum.workfolio.utils.TimeUtil

fun Link.toProto(): com.spectrum.workfolio.proto.common.Link {
    val builder = com.spectrum.workfolio.proto.common.Link.newBuilder()

    builder.setId(this.id)
    builder.setUrl(this.url)
    builder.setIsVisible(this.isVisible)
    builder.setResume(this.resume.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun Link.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.Link {
    val builder = com.spectrum.workfolio.proto.common.Link.newBuilder()

    builder.setId(this.id)
    builder.setUrl(this.url)
    builder.setIsVisible(this.isVisible)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
