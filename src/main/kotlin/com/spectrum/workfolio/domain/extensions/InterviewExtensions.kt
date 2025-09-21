package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.interview.Interview
import com.spectrum.workfolio.utils.TimeUtil

fun Interview.toProto(): com.spectrum.workfolio.proto.common.Interview {
    val builder = com.spectrum.workfolio.proto.common.Interview.newBuilder()

    builder.setId(this.id)
    builder.setMemo(this.memo)
    builder.setType(com.spectrum.workfolio.proto.common.Interview.Type.valueOf(this.type.name))
    builder.setJobSearchCompany(this.jobSearchCompany.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.startedAt != null) { builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!)) }
    if (this.endedAt != null) { builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!)) }
    if (this.title != null) { builder.setTitle(this.title!!) }


    return builder.build()
}
