package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Activity
import com.spectrum.workfolio.utils.TimeUtil

fun Activity.toProto(): com.spectrum.workfolio.proto.common.Activity {
    val builder = com.spectrum.workfolio.proto.common.Activity.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)

    builder.setDescription(this.description)
    builder.setOrganization(this.organization)
    builder.setCertificateNumber(this.certificateNumber)
    builder.setIsVisible(this.isVisible)
    builder.setResume(this.resume.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.type != null) {
        builder.setType(
            com.spectrum.workfolio.proto.common.Activity.ActivityType.valueOf(this.type!!.name),
        )
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}

fun Activity.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.Activity {
    val builder = com.spectrum.workfolio.proto.common.Activity.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setDescription(this.description)
    builder.setOrganization(this.organization)
    builder.setCertificateNumber(this.certificateNumber)
    builder.setIsVisible(this.isVisible)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.type != null) {
        builder.setType(
            com.spectrum.workfolio.proto.common.Activity.ActivityType.valueOf(this.type!!.name),
        )
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
