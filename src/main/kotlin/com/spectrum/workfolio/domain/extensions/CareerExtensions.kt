package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.resume.Career
import com.spectrum.workfolio.utils.TimeUtil

fun Career.toProto(): com.spectrum.workfolio.proto.common.Career {
    val builder = com.spectrum.workfolio.proto.common.Career.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    builder.setPosition(this.position)
    builder.setDepartment(this.department)
    builder.setSalary(this.salary ?: 0)
    builder.setJobGrade(this.jobGrade)
    builder.setJob(this.job)
    builder.setResume(this.resume.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    builder.addAllAchievements(achievements.map { it.toProtoWithoutCareer() })
    builder.addAllSalaries(salaries.map { it.toProtoWithoutCareer() })

    if (this.employmentType != null) {
        builder.setEmploymentType(
            com.spectrum.workfolio.proto.common.Career.EmploymentType.valueOf(this.employmentType!!.name),
        )
    }
    if (this.isWorking != null) {
        builder.setIsWorking(this.isWorking!!)
    }
    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}

fun Career.toProtoWithoutResume(): com.spectrum.workfolio.proto.common.Career {
    val builder = com.spectrum.workfolio.proto.common.Career.newBuilder()

    builder.setId(id)
    builder.setName(name)
    builder.setPosition(position)
    builder.setDepartment(department)
    builder.setSalary(salary ?: 0)
    builder.setJobGrade(jobGrade)
    builder.setJob(job)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))
    builder.addAllAchievements(achievements.map { it.toProtoWithoutCareer() })
    builder.addAllSalaries(salaries.map { it.toProtoWithoutCareer() })

    if (this.employmentType != null) {
        builder.setEmploymentType(
            com.spectrum.workfolio.proto.common.Career.EmploymentType.valueOf(this.employmentType!!.name),
        )
    }
    if (this.isWorking != null) {
        builder.setIsWorking(this.isWorking!!)
    }
    if (this.isVisible != null) {
        builder.setIsVisible(this.isVisible!!)
    }
    if (this.startedAt != null) {
        builder.setStartedAt(TimeUtil.toEpochMilli(this.startedAt!!))
    }
    if (this.endedAt != null) {
        builder.setEndedAt(TimeUtil.toEpochMilli(this.endedAt!!))
    }

    return builder.build()
}
