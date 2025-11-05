package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.ApplicationStage
import com.spectrum.workfolio.utils.TimeUtil

fun ApplicationStage.toProto(): com.spectrum.workfolio.proto.common.ApplicationStage {
    val builder = com.spectrum.workfolio.proto.common.ApplicationStage.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStatus(
        com.spectrum.workfolio.proto.common.ApplicationStage.ApplicationStageStatus.valueOf(this.status.name),
    )
    builder.setMemo(this.memo)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setJobApplication(this.jobApplication.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }

    return builder.build()
}

fun ApplicationStage.toWithoutJobApplicationProto(): com.spectrum.workfolio.proto.common.ApplicationStage {
    val builder = com.spectrum.workfolio.proto.common.ApplicationStage.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStatus(
        com.spectrum.workfolio.proto.common.ApplicationStage.ApplicationStageStatus.valueOf(this.status.name),
    )
    builder.setMemo(this.memo)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }

    return builder.build()
}
