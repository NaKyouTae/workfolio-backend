package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Education
import com.spectrum.workfolio.utils.TimeUtil

fun Education.toProto(): com.spectrum.workfolio.proto.common.Education {
    val builder = com.spectrum.workfolio.proto.common.Education.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setMajor(this.major)
    builder.setDescription(this.description)
    builder.setIsVisible(this.isVisible)
    builder.setResume(this.resume.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.status != null) {
        builder.setStatus(
            com.spectrum.workfolio.proto.common.Education.EducationStatus.valueOf(this.status!!.name),
        )
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }

    return builder.build()
}

fun Education.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.Education {
    val builder = com.spectrum.workfolio.proto.common.Education.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setMajor(this.major)
    builder.setDescription(this.description)
    builder.setIsVisible(this.isVisible)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.status != null) {
        builder.setStatus(
            com.spectrum.workfolio.proto.common.Education.EducationStatus.valueOf(this.status!!.name),
        )
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }

    return builder.build()
}
