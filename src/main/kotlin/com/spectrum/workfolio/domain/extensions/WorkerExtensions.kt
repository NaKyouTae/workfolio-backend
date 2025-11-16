package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.utils.TimeUtil

fun Worker.toProto(): com.spectrum.workfolio.proto.common.Worker {
    val builder = com.spectrum.workfolio.proto.common.Worker.newBuilder()

    builder.setId(this.id)
    builder.setNickName(this.nickName)
    builder.setEmail(this.email)
    builder.setPhone(this.phone)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.birthDate != null) {
        builder.setBirthDate(TimeUtil.toEpochMilli(this.birthDate!!))
    }

    if (this.gender != null) {
        builder.setGender(com.spectrum.workfolio.proto.common.Worker.Gender.valueOf(this.gender!!.name))
    }

    return builder.build()
}
