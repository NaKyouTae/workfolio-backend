package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import com.spectrum.workfolio.utils.TimeUtil

fun TurnOverRetrospective.toProto(): com.spectrum.workfolio.proto.common.TurnOverRetrospective {
    val builder = com.spectrum.workfolio.proto.common.TurnOverRetrospective.newBuilder()

    builder.setId(this.id)
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

    builder.setTurnOver(this.turnOver.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

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

fun TurnOverRetrospective.toWithoutTurnOverProto(): com.spectrum.workfolio.proto.common.TurnOverRetrospective {
    val builder = com.spectrum.workfolio.proto.common.TurnOverRetrospective.newBuilder()

    builder.setId(this.id)
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

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

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
