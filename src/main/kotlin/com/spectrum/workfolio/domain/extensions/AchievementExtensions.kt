package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Achievement
import com.spectrum.workfolio.utils.TimeUtil

fun Achievement.toProto(): com.spectrum.workfolio.proto.common.Achievement {
    val builder = com.spectrum.workfolio.proto.common.Achievement.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setRole(this.role)
    builder.setDescription(this.description)

    builder.setCareer(this.career.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}

fun Achievement.toProtoWithoutCareer(): com.spectrum.workfolio.proto.common.Achievement {
    val builder = com.spectrum.workfolio.proto.common.Achievement.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setRole(this.role)
    builder.setDescription(this.description)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
