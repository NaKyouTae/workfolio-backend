package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Project
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.entity.resume.Salary
import com.spectrum.workfolio.utils.TimeUtil

fun Project.toProto(): com.spectrum.workfolio.proto.common.Project {
    val builder = com.spectrum.workfolio.proto.common.Project.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setDescription(this.description)
    builder.setIsVisible(this.isVisible)
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setCompany(this.company.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
