package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Salary
import com.spectrum.workfolio.utils.TimeUtil

fun Salary.toProto(): com.spectrum.workfolio.proto.common.Salary {
    val builder = com.spectrum.workfolio.proto.common.Salary.newBuilder()

    builder.setId(this.id)
    builder.setAmount(this.amount ?: 0)
    builder.setMemo(this.memo)
    builder.setCareer(this.career.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.negotiationDate != null) {
        builder.setNegotiationDate(TimeUtil.toEpochMilli(this.negotiationDate!!))
    }
    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }

    return builder.build()
}

fun Salary.toProtoWithoutCareer(): com.spectrum.workfolio.proto.common.Salary {
    val builder = com.spectrum.workfolio.proto.common.Salary.newBuilder()

    builder.setId(this.id)
    builder.setAmount(this.amount ?: 0)
    builder.setMemo(this.memo)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.negotiationDate != null) {
        builder.setNegotiationDate(TimeUtil.toEpochMilli(this.negotiationDate!!))
    }
    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }

    return builder.build()
}
