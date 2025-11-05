package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.SelfIntroduction
import com.spectrum.workfolio.utils.TimeUtil

fun SelfIntroduction.toProto(): com.spectrum.workfolio.proto.common.SelfIntroduction {
    val builder = com.spectrum.workfolio.proto.common.SelfIntroduction.newBuilder()

    builder.setId(this.id)
    builder.setQuestion(this.question)
    builder.setContent(content)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setTurnOverGoal(this.turnOverGoal.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun SelfIntroduction.toWithoutTurnOverGoalProto(): com.spectrum.workfolio.proto.common.SelfIntroduction {
    val builder = com.spectrum.workfolio.proto.common.SelfIntroduction.newBuilder()

    builder.setId(this.id)
    builder.setQuestion(this.question)
    builder.setContent(content)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
