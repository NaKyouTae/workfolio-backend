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
 * 이력서
 */
@Entity
@Table(
    name = "projects",
    indexes = [
        Index(name = "idx_projects_company_id", columnList = "company_id"),
    ],
)
class Project(
    title: String,
    description: String,
    isVisible: Boolean,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    company: Company,
) : BaseEntity("RS") {
    @Column(name = "title", length = 1024, nullable = false)
    var title: String = title
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String = description
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
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company = company

    fun changeInfo(
        title: String,
        description: String,
        isVisible: Boolean,
        startedAt: LocalDate,
        endedAt: LocalDate? = null
    ) {
        this.title = title
        this.description = description
        this.isVisible = isVisible
        this.startedAt = startedAt
        this.endedAt = endedAt
    }
}
