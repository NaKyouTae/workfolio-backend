package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Project
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.proto.common.Project as ProtoProject

/**
 * Project 엔티티를 Proto 메시지로 변환하는 확장 함수
 */
fun Project.toProto(): ProtoProject {
    return ProtoProject.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setDescription(this.description)
        .setIsVisible(this.isVisible)
        .setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
        .setEndedAt(if (this.endedAt != null) TimeUtil.toEpochMilli(this.endedAt!!) else 0)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}

/**
 * Project 엔티티를 Career 없이 Proto 메시지로 변환하는 확장 함수
 */
fun Project.toProtoWithoutCareer(): ProtoProject {
    return ProtoProject.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setDescription(this.description)
        .setIsVisible(this.isVisible)
        .setStartedAt(TimeUtil.toEpochMilli(this.startedAt))
        .setEndedAt(if (this.endedAt != null) TimeUtil.toEpochMilli(this.endedAt!!) else 0)
        .setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
        .setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
        .build()
}
