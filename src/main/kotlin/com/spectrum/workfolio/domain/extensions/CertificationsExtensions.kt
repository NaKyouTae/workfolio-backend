package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Certifications
import com.spectrum.workfolio.utils.TimeUtil

fun Certifications.toProto(): com.spectrum.workfolio.proto.common.Certifications {
    val builder = com.spectrum.workfolio.proto.common.Certifications.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setNumber(this.number)
    builder.setIssuer(this.issuer)
    builder.setIssuedAt(TimeUtil.toEpochMilli(this.issuedAt))
    builder.setResume(this.resume.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.expirationPeriod != null) {
        builder.setExpirationPeriod(TimeUtil.toEpochMilli(this.expirationPeriod!!))
    }

    return builder.build()
}
