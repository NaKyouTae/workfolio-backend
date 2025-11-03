package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.utils.TimeUtil

fun TurnOverGoal.toProto(): com.spectrum.workfolio.proto.common.TurnOverGoal {
    val builder = com.spectrum.workfolio.proto.common.TurnOverGoal.newBuilder()

    builder.setId(this.id)
    builder.setReason(this.reason)
    builder.setGoal(this.goal)

    builder.setTurnOver(this.turnOver.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun TurnOverGoal.toDetailProto(): com.spectrum.workfolio.proto.common.TurnOverGoalDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverGoalDetail.newBuilder()

    builder.setId(this.id)
    builder.setReason(this.reason)
    builder.setGoal(this.goal)

    builder.addAllSelfIntroductions(this.selfIntroductions.map { it.toWithoutTurnOverGoalProto() })
    builder.addAllInterviewQuestions(this.interviewQuestions.map { it.toWithoutTurnOverGoalProto() })
    builder.addAllCheckList(this.checkList.map { it.toWithoutTurnOverGoalProto() })

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
