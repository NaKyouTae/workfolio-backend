package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.InterviewQuestion
import com.spectrum.workfolio.utils.TimeUtil

fun InterviewQuestion.toProto(): com.spectrum.workfolio.proto.common.InterviewQuestion {
    val builder = com.spectrum.workfolio.proto.common.InterviewQuestion.newBuilder()

    builder.setId(this.id)
    builder.setQuestion(this.question)
    builder.setAnswer(this.answer)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setTurnOverGoal(this.turnOverGoal.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun InterviewQuestion.toWithoutTurnOverGoalProto(): com.spectrum.workfolio.proto.common.InterviewQuestion {
    val builder = com.spectrum.workfolio.proto.common.InterviewQuestion.newBuilder()

    builder.setId(this.id)
    builder.setQuestion(this.question)
    builder.setAnswer(this.answer)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
