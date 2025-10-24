package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Project
import com.spectrum.workfolio.utils.TimeUtil

fun Project.toProto(): com.spectrum.workfolio.proto.common.Project {
    val builder = com.spectrum.workfolio.proto.common.Project.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setRole(this.role)
    builder.setDescription(this.description)
    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)
    builder.setResume(this.resume.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}

fun Project.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.Project {
    val builder = com.spectrum.workfolio.proto.common.Project.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setRole(this.role)
    builder.setDescription(this.description)
    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
