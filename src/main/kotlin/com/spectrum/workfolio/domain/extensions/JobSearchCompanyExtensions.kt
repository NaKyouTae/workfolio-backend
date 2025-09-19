package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.interview.JobSearchCompany
import com.spectrum.workfolio.utils.TimeUtil

fun JobSearchCompany.toProto(): com.spectrum.workfolio.proto.common.JobSearchCompany {
    val builder = com.spectrum.workfolio.proto.common.JobSearchCompany.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStatus(com.spectrum.workfolio.proto.common.JobSearchCompany.Status.valueOf(this.status.name))
    builder.setLink(this.link)
    builder.setJobSearch(this.jobSearch.toProto())
    builder.setAppliedAt(TimeUtil.toEpochMilli(this.appliedAt))
    builder.setClosedAt(TimeUtil.toEpochMilli(this.closedAt))
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
