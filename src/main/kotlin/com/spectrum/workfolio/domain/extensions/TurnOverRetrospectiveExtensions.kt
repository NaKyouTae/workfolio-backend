package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.entity.common.Memo
import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import com.spectrum.workfolio.utils.TimeUtil

fun TurnOverRetrospective.toProto(turnOverId: String): com.spectrum.workfolio.proto.common.TurnOverRetrospective {
    val builder = com.spectrum.workfolio.proto.common.TurnOverRetrospective.newBuilder()

    builder.setId(turnOverId)
    builder.setName(this.name)
    builder.setSalary(this.salary)
    builder.setPosition(this.position)
    builder.setJobTitle(this.jobTitle)
    builder.setRank(this.rank)
    builder.setDepartment(this.department)
    builder.setReason(this.reason)
    builder.setScore(this.score)
    builder.setReviewSummary(this.reviewSummary)
    builder.setWorkType(this.workType)

    if (this.joinedAt != null) {
        builder.setJoinedAt(TimeUtil.toEpochMilli(this.joinedAt!!))
    }
    if (this.employmentType != null) {
        builder.setEmploymentType(
            com.spectrum.workfolio.proto.common.TurnOverRetrospective.EmploymentType.valueOf(this.employmentType!!.name),
        )
    }

    return builder.build()
}

fun TurnOverRetrospective.toDetailProto(memos: List<Memo>, attachments: List<Attachment>, turnOverId: String): com.spectrum.workfolio.proto.common.TurnOverRetrospectiveDetail {
    val builder = com.spectrum.workfolio.proto.common.TurnOverRetrospectiveDetail.newBuilder()

    builder.setId(turnOverId)
    builder.setName(this.name)
    builder.setSalary(this.salary)
    builder.setPosition(this.position)
    builder.setJobTitle(this.jobTitle)
    builder.setRank(this.rank)
    builder.setDepartment(this.department)
    builder.setReason(this.reason)
    builder.setScore(this.score)
    builder.setReviewSummary(this.reviewSummary)
    builder.setWorkType(this.workType)

    builder.addAllMemos(memos.map { it.toProto() })
    builder.addAllAttachments(attachments.map { it.toProto() })

    if (this.joinedAt != null) {
        builder.setJoinedAt(TimeUtil.toEpochMilli(this.joinedAt!!))
    }
    if (this.employmentType != null) {
        builder.setEmploymentType(
            com.spectrum.workfolio.proto.common.TurnOverRetrospectiveDetail.EmploymentType.valueOf(
                this.employmentType!!.name,
            ),
        )
    }

    return builder.build()
}
