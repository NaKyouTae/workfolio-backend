package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.utils.TimeUtil

fun Worker.toProto(): com.spectrum.workfolio.proto.common.Worker {
    return com.spectrum.workfolio.proto.common.Worker.newBuilder()
        .setId(this.id)
        .setNickName(this.nickName)
        .setEmail(this.email)
        .setPhone(this.phone)
        .setBirthDate(TimeUtil.toEpochMilli(this.birthDate))
        .setGender(com.spectrum.workfolio.proto.common.Worker.Gender.valueOf(this.gender.name))
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
