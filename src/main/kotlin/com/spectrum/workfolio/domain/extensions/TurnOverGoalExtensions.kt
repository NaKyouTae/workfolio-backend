package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.entity.common.Memo
import com.spectrum.workfolio.domain.entity.turnover.CheckList
import com.spectrum.workfolio.domain.entity.turnover.InterviewQuestion
import com.spectrum.workfolio.domain.entity.turnover.SelfIntroduction
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal

fun TurnOverGoal.toProto(turnOverId: String): com.spectrum.workfolio.proto.common.TurnOverGoal {
    val builder = com.spectrum.workfolio.proto.common.TurnOverGoal.newBuilder()

    builder.setId(turnOverId)
    builder.setReason(this.reason)
    builder.setGoal(this.goal)

    return builder.build()
}

fun TurnOverGoal.toDetailProto(
    selfIntroductions: List<SelfIntroduction>,
    interviewQuestions: List<InterviewQuestion>,
    checkLists: List<CheckList>,
    memos: List<Memo>,
    attachments: List<Attachment>,
    turnOverId: String,
): com.spectrum.workfolio.proto.common.TurnOverGoalDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverGoalDetail.newBuilder()

    val selfIntroductionProtos = selfIntroductions.map { it.toWithoutTurnOverGoalProto() }.sortedBy { it.priority }
    val interviewQuestionProtos = interviewQuestions.map { it.toWithoutTurnOverGoalProto() }.sortedBy { it.priority }
    val checkListProtos = checkLists.map { it.toWithoutTurnOverGoalProto() }.sortedBy { it.priority }

    builder.setId(turnOverId)
    builder.setReason(this.reason)
    builder.setGoal(this.goal)
    builder.addAllSelfIntroductions(selfIntroductionProtos)
    builder.addAllInterviewQuestions(interviewQuestionProtos)
    builder.addAllCheckList(checkListProtos)
    builder.addAllMemos(memos.map { it.toProto() })
    builder.addAllAttachments(attachments.map { it.toProto() })

    return builder.build()
}
