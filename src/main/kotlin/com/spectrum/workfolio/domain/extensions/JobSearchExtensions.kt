package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.interview.JobSearch
import com.spectrum.workfolio.utils.TimeUtil

fun JobSearch.toProto(): com.spectrum.workfolio.proto.common.JobSearch {
    val builder = com.spectrum.workfolio.proto.common.JobSearch.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setMemo(this.memo)
    builder.setWorker(this.worker.toProto())
    builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }
    if (this.prevCompany != null) {
        builder.setPrevCompany(this.prevCompany!!.toProto())
    }
    if (this.nextCompany != null) {
        builder.setNextCompany(this.nextCompany!!.toProto())
    }

    return builder.build()
}
