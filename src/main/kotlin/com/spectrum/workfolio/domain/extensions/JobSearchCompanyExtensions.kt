package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.interview.JobSearchCompany
import com.spectrum.workfolio.utils.TimeUtil

fun JobSearchCompany.toProto(): com.spectrum.workfolio.proto.common.JobSearchCompany {
    val builder = com.spectrum.workfolio.proto.common.JobSearchCompany.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setStatus(com.spectrum.workfolio.proto.common.JobSearchCompany.Status.valueOf(this.status.name))
    builder.setJobSearch(this.jobSearch.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.appliedAt != null) { builder.setAppliedAt(TimeUtil.toEpochMilli(this.appliedAt!!)) }
    if (this.closedAt != null) { builder.setClosedAt(TimeUtil.toEpochMilli(this.closedAt!!)) }
    if (this.endedAt != null) { builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!)) }
    if (this.industry != null) { builder.setIndustry(this.industry) }
    if (this.location != null) { builder.setLocation(this.location) }
    if (this.businessSize != null) { builder.setBusinessSize(this.businessSize) }
    if (this.description != null) { builder.setDescription(this.description) }
    if (this.memo != null) { builder.setMemo(this.memo) }
    if (this.link != null) { builder.setLink(this.link) }

    return builder.build()
}
