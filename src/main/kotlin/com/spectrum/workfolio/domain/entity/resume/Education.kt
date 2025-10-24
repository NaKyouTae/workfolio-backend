package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.EducationStatus
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
 * 학력
 */
@Entity
@Table(
    name = "educations",
    indexes = [
        Index(name = "idx_educations_resume_id", columnList = "resume_id"),
    ],
)
class Education(
    name: String,
    major: String,
    description: String,
    endedAt: LocalDate? = null,
    startedAt: LocalDate? = null,
    status: EducationStatus? = null,
    isVisible: Boolean,
    resume: Resume,
) : BaseEntity("ED") {

    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "major", length = 1024, nullable = false)
    var major: String = major
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String = description
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = true)
    var status: EducationStatus? = status
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    fun changeInfo(
        name: String,
        major: String,
        description: String,
        endedAt: LocalDate? = null,
        startedAt: LocalDate? = null,
        status: EducationStatus? = null,
        isVisible: Boolean,
    ) {
        this.name = name
        this.major = major
        this.description = description
        this.status = status
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.isVisible = isVisible
    }
}
