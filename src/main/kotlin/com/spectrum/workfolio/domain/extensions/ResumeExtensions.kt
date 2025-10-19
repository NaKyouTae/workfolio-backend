package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.entity.resume.Salary
import com.spectrum.workfolio.utils.TimeUtil

fun Resume.toProto(): com.spectrum.workfolio.proto.common.Resume {
    val builder = com.spectrum.workfolio.proto.common.Resume.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setDescription(this.description)
    builder.setPhone(this.phone)
    builder.setEmail(this.email)
    builder.setIsPublic(this.isPublic)
    builder.setIsDefault(this.isDefault)
    builder.setPublicId(this.publicId)
    builder.setWorker(this.worker.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
