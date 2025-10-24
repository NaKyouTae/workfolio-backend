package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.ActivityType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 활동
 */
@Entity
@Table(
    name = "activities",
    indexes = [
        Index(name = "idx_activities_resume_id_priority", columnList = "resume_id, priority"),
    ],
)
class Activity(
    name: String,
    description: String,
    organization: String,
    certificateNumber: String,
    isVisible: Boolean,
    priority: Int = 0,
    endedAt: LocalDate? = null,
    type: ActivityType? = null,
    startedAt: LocalDate? = null,
    resume: Resume,
) : BaseEntity("AT") {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = true)
    var type: ActivityType? = type
        protected set

    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "organization", length = 1024, nullable = false)
    var organization: String = organization
        protected set

    @Column(name = "certificate_number", length = 512, nullable = false)
    var certificateNumber: String = certificateNumber
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String = description
        protected set

    @Column(name = "started_at", nullable = true)
    var startedAt: LocalDate? = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    fun changeInfo(
        name: String,
        description: String,
        organization: String,
        certificateNumber: String,
        isVisible: Boolean,
        priority: Int = 0,
        endedAt: LocalDate? = null,
        type: ActivityType? = null,
        startedAt: LocalDate? = null,
    ) {
        this.type = type
        this.name = name
        this.organization = organization
        this.certificateNumber = certificateNumber
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.description = description
        this.isVisible = isVisible
        this.priority = priority
    }
}
