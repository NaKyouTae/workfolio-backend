package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.DegreesStatus
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
 * 학위
 */
@Entity
@Table(
    name = "degrees",
    indexes = [
        Index(name = "idx_degrees_name", columnList = "name"),
        Index(name = "idx_degrees_resume_id", columnList = "resume_id"),
    ],
)
class Degrees(
    name: String,
    major: String,
    status: DegreesStatus,
    isVisible: Boolean = false,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    resume: Resume,
) : BaseEntity("DE") {

    @Column(name = "name", length = 256, nullable = false)
    var name: String = name
        protected set

    @Column(name = "major", length = 1024, nullable = false)
    var major: String = major
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: DegreesStatus = status
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    fun changeInfo(name: String, major: String, status: DegreesStatus, startedAt: LocalDate, endedAt: LocalDate?) {
        this.name = name
        this.major = major
        this.status = status
        this.startedAt = startedAt
        this.endedAt = endedAt
    }
}
