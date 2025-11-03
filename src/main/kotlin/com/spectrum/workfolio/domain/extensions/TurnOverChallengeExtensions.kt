package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.utils.TimeUtil

fun TurnOverChallenge.toProto(): com.spectrum.workfolio.proto.common.TurnOverChallenge {
    val builder = com.spectrum.workfolio.proto.common.TurnOverChallenge.newBuilder()

    builder.setId(this.id)

    builder.setTurnOver(this.turnOver.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun TurnOverChallenge.toDetailProto(): com.spectrum.workfolio.proto.common.TurnOverChallengeDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverChallengeDetail.newBuilder()

    builder.setId(this.id)

    builder.addAllJobApplications(this.jobApplications.map { it.toWithoutTurnOverGoalProto() })

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
