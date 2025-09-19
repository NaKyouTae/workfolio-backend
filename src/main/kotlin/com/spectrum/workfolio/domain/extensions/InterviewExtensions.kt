package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.interview.Interview
import com.spectrum.workfolio.utils.TimeUtil

fun Interview.toProto(): com.spectrum.workfolio.proto.common.Interview {
    val builder = com.spectrum.workfolio.proto.common.Interview.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setMemo(this.memo)
    builder.setPrevCompany(this.prevCompany.toProto())
    builder.setWorker(this.worker.toProto())
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }
    if (this.nextCompany != null) {
        builder.setNextCompany(this.nextCompany!!.toProto())
    }

    return builder.build()
}
