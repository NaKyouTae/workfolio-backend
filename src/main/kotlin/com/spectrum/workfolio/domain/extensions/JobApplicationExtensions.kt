package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.utils.TimeUtil

fun JobApplication.toProto(): com.spectrum.workfolio.proto.common.JobApplication {
    val builder = com.spectrum.workfolio.proto.common.JobApplication.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setPosition(this.position)
    builder.setJobPostingUrl(this.jobPostingUrl)
    builder.setJobPostingTitle(this.jobPostingTitle)
    builder.setApplicationSource(this.applicationSource)
    builder.setMemo(this.memo)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setTurnOverChallenge(this.turnOverChallenge.toProto())

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

fun JobApplication.toDetailProto(): com.spectrum.workfolio.proto.common.JobApplicationDetail {
    val builder = com.spectrum.workfolio.proto.common.JobApplicationDetail.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setPosition(this.position)
    builder.setJobPostingUrl(this.jobPostingUrl)
    builder.setJobPostingTitle(this.jobPostingTitle)
    builder.setApplicationSource(this.applicationSource)
    builder.setMemo(this.memo)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.addAllApplicationStages(this.applicationStages.map { it.toWithoutJobApplicationProto() }.sortedBy { it.priority })

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
