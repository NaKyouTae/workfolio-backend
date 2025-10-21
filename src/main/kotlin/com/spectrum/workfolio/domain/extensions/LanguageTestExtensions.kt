package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.LanguageTest
import com.spectrum.workfolio.utils.TimeUtil

fun LanguageTest.toProto(): com.spectrum.workfolio.proto.common.LanguageTest {
    val builder = com.spectrum.workfolio.proto.common.LanguageTest.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setScore(this.score)

    builder.setLanguageSkill(this.languageSkill.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }
    if (this.acquiredAt != null) {
        builder.setAcquiredAt(TimeUtil.toEpochMilli(this.acquiredAt!!))
    }

    return builder.build()
}

fun LanguageTest.toProtoWithoutSkill(): com.spectrum.workfolio.proto.common.LanguageTest {
    val builder = com.spectrum.workfolio.proto.common.LanguageTest.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setScore(this.score)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }
    if (this.acquiredAt != null) {
        builder.setAcquiredAt(TimeUtil.toEpochMilli(this.acquiredAt!!))
    }

    return builder.build()
}
