package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import com.spectrum.workfolio.utils.TimeUtil

fun TurnOver.toProto(): com.spectrum.workfolio.proto.common.TurnOver {
    val builder = com.spectrum.workfolio.proto.common.TurnOver.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt))
    }

    builder.setWorker(this.worker.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun TurnOver.toDetailProto(
    turnOverGoal: com.spectrum.workfolio.proto.common.TurnOverGoalDetail,
    turnOverChallenge: com.spectrum.workfolio.proto.common.TurnOverChallengeDetail,
    turnOverRetrospective: com.spectrum.workfolio.proto.common.TurnOverRetrospectiveDetail,
): com.spectrum.workfolio.proto.common.TurnOverDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverDetail.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt))
    }

    builder.setWorker(this.worker.toProto())
    builder.setTurnOverGoal(turnOverGoal)
    builder.setTurnOverChallenge(turnOverChallenge)
    builder.setTurnOverRetrospective(turnOverRetrospective)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
