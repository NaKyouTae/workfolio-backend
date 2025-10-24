package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 주요 성과
 */
@Entity
@Table(
    name = "projects",
    indexes = [
        Index(name = "idx_projects_resume_id_priority", columnList = "resume_id, priority"),
    ],
)
class Project(
    title: String,
    role: String,
    description: String,
    startedAt: LocalDate? = null,
    endedAt: LocalDate? = null,
    isVisible: Boolean,
    priority: Int = 0,
    resume: Resume,
) : BaseEntity("AC") {

    @Column(name = "title", length = 1024, nullable = true)
    var title: String? = title
        protected set

    @Column(name = "role", length = 512, nullable = true)
    var role: String? = role
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    var description: String? = description
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
        title: String,
        role: String,
        description: String,
        startedAt: LocalDate? = null,
        endedAt: LocalDate? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ) {
        this.title = title
        this.role = role
        this.description = description
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.isVisible = isVisible
        this.priority = priority
    }
}
