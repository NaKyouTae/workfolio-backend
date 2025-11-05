package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.entity.common.Memo
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.utils.TimeUtil

fun TurnOverGoal.toProto(): com.spectrum.workfolio.proto.common.TurnOverGoal {
    val builder = com.spectrum.workfolio.proto.common.TurnOverGoal.newBuilder()

    builder.setId(this.id)
    builder.setReason(this.reason)
    builder.setGoal(this.goal)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun TurnOverGoal.toDetailProto(memos: List<Memo>, attachments: List<Attachment>): com.spectrum.workfolio.proto.common.TurnOverGoalDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverGoalDetail.newBuilder()

    builder.setId(this.id)
    builder.setReason(this.reason)
    builder.setGoal(this.goal)

    builder.addAllSelfIntroductions(this.selfIntroductions.map { it.toWithoutTurnOverGoalProto() }.sortedBy { it.priority })
    builder.addAllInterviewQuestions(this.interviewQuestions.map { it.toWithoutTurnOverGoalProto() }.sortedBy { it.priority })
    builder.addAllCheckList(this.checkList.map { it.toWithoutTurnOverGoalProto() }.sortedBy { it.priority })
    builder.addAllMemos(memos.map { it.toProto() })
    builder.addAllAttachments(attachments.map { it.toProto() })

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
