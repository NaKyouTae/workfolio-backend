package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import com.spectrum.workfolio.utils.TimeUtil

fun TurnOver.toProto(): com.spectrum.workfolio.proto.common.TurnOver {
    val builder = com.spectrum.workfolio.proto.common.TurnOver.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)

    builder.setWorker(this.worker.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun TurnOver.toDetailProto(): com.spectrum.workfolio.proto.common.TurnOverDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverDetail.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)

    builder.setWorker(this.worker.toProto())
    builder.setTurnOverGoal(this.turnOverGoal.toDetailProto())
    builder.setTurnOverChallenge(this.turnOverChallenge.toDetailProto())
    builder.setTurnOverRetrospective(this.turnOverRetrospective.toWithoutTurnOverProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
