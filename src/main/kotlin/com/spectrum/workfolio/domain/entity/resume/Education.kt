package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 교육
 */
@Entity
@Table(
    name = "educations",
    indexes = [
        Index(name = "idx_educations_name", columnList = "name"),
        Index(name = "idx_educations_resume_id", columnList = "resume_id"),
    ],
)
class Education(
    name: String,
    isVisible: Boolean = false,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    agency: String,
    resume: Resume,
) : BaseEntity("ED") {
    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "agency", nullable = false)
    var agency: String = agency
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    fun changeInfo(name: String, startedAt: LocalDate, endedAt: LocalDate?, agency: String) {
        this.name = name
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.agency = agency
    }
}
