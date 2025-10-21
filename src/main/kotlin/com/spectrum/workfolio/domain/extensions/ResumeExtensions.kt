package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.*
import com.spectrum.workfolio.proto.resume.*
import com.spectrum.workfolio.utils.TimeUtil

/**
 * Resume 엔티티를 Proto 메시지로 변환하는 Extension 함수들
 */

fun Resume.toProto(): com.spectrum.workfolio.proto.common.Resume {
    val builder = com.spectrum.workfolio.proto.common.Resume.newBuilder()

    builder.setId(this.id)
    builder.setTitle(this.title)
    builder.setDescription(this.name)
    builder.setPhone(this.phone)
    builder.setEmail(this.email)
    builder.setPublicId(this.publicId)

    builder.setWorker(this.worker.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    if (this.isPublic != null) builder.setIsPublic(this.isPublic!!)
    if (this.isDefault != null) builder.setIsDefault(this.isDefault!!)
    if (this.birthDate != null) builder.setBrithDate(TimeUtil.toEpochMilli(this.birthDate!!))
    if (this.gender != null) {
        builder.setGender(
            com.spectrum.workfolio.proto.common.Resume.Gender.valueOf(this.gender!!.name)
        )
    }

    return builder.build()
}

fun Resume.toDetailProto(): com.spectrum.workfolio.proto.common.ResumeDetail {
    val builder = com.spectrum.workfolio.proto.common.ResumeDetail.newBuilder()

    builder.setResume(this.toProto())
    builder.addAllCareers(this.careers.map { it.toProtoWithoutResume() })
    builder.addAllEducations(this.educations.map { it.toProtoWithoutResume() })
    builder.addAllActivities(this.activities.map { it.toProtoWithoutResume() })
    builder.addAllLanguageSkills(this.languageSkills.map { it.toProtoWithoutResume() })
    builder.addAllAttachments(this.attachments.map { it.toProtoWithoutResume() })

    return builder.build()
}
