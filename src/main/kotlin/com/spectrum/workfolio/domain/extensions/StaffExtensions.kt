package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.Staff
import com.spectrum.workfolio.utils.TimeUtil

fun Staff.toProto(): com.spectrum.workfolio.proto.common.Staff {
    val builder = com.spectrum.workfolio.proto.common.Staff.newBuilder()

    builder.setId(this.id)
    builder.setUsername(this.username)
    builder.setName(this.name)
    builder.setEmail(this.email)
    if (this.phone != null) {
        builder.setPhone(this.phone)
    }
    builder.setIsActive(this.isActive)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

