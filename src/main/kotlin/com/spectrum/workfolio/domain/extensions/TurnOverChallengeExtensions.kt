package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.entity.common.Memo
import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge

fun TurnOverChallenge.toProto(turnOverId: String): com.spectrum.workfolio.proto.common.TurnOverChallenge {
    val builder = com.spectrum.workfolio.proto.common.TurnOverChallenge.newBuilder()

    builder.setId(turnOverId)

    return builder.build()
}

fun TurnOverChallenge.toDetailProto(
    jobApplications: List<JobApplication>,
    memos: List<Memo>,
    attachments: List<Attachment>,
    turnOverId: String,
): com.spectrum.workfolio.proto.common.TurnOverChallengeDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverChallengeDetail.newBuilder()

    builder.setId(turnOverId)

    builder.addAllJobApplications(jobApplications.map { it.toDetailProto() }.sortedBy { it.priority })
    builder.addAllMemos(memos.map { it.toProto() })
    builder.addAllAttachments(attachments.map { it.toProto() })

    return builder.build()
}
