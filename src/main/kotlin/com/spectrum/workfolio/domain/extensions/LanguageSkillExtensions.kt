package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.LanguageSkill
import com.spectrum.workfolio.utils.TimeUtil

fun LanguageSkill.toProto(): com.spectrum.workfolio.proto.common.LanguageSkill {
    val builder = com.spectrum.workfolio.proto.common.LanguageSkill.newBuilder()

    builder.setId(this.id)
    builder.setIsVisible(this.isVisible)
    builder.setResume(this.resume.toProto())
    builder.addAllLanguageTests(this.languageTests.map { it.toProtoWithoutSkill() })

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.language != null) {
        builder.setLanguage(
            com.spectrum.workfolio.proto.common.LanguageSkill.Language.valueOf(this.language!!.name),
        )
    }
    if (this.level != null) {
        builder.setLevel(
            com.spectrum.workfolio.proto.common.LanguageSkill.LanguageLevel.valueOf(this.level!!.name),
        )
    }

    return builder.build()
}

fun LanguageSkill.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.LanguageSkill {
    val builder = com.spectrum.workfolio.proto.common.LanguageSkill.newBuilder()

    builder.setId(this.id)
    builder.setIsVisible(this.isVisible)
    builder.addAllLanguageTests(this.languageTests.map { it.toProtoWithoutSkill() })
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.language != null) {
        builder.setLanguage(
            com.spectrum.workfolio.proto.common.LanguageSkill.Language.valueOf(this.language!!.name),
        )
    }
    if (this.level != null) {
        builder.setLevel(
            com.spectrum.workfolio.proto.common.LanguageSkill.LanguageLevel.valueOf(this.level!!.name),
        )
    }

    return builder.build()
}
