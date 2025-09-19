package com.spectrum.workfolio.domain.entity.interview

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.JobSearchCompanyStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "job_search_company",
    indexes = [
        Index(name = "idx_job_search_company_job_search_id", columnList = "job_search_id"),
    ],
)
class JobSearchCompany(
    name: String,
    status: JobSearchCompanyStatus,
    appliedAt: LocalDateTime,
    closedAt: LocalDateTime,
    endedAt: LocalDate? = null,
    link: String? = null,
    jobSearch: JobSearch,
) : BaseEntity("JC") {

    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "status", length = 128, nullable = false)
    var status: JobSearchCompanyStatus = status
        protected set

    @Column(name = "applied_at", nullable = false)
    var appliedAt: LocalDateTime = appliedAt
        protected set

    @Column(name = "closed_at", nullable = false)
    var closedAt: LocalDateTime = closedAt
        protected set

    @Column(name = "ended_at", nullable = false)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "link", columnDefinition = "TEXT", nullable = true)
    var link: String? = link
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_search_id", nullable = false)
    var jobSearch: JobSearch = jobSearch
        protected set

    fun changeInfo(
        name: String,
        status: JobSearchCompanyStatus,
        appliedAt: LocalDateTime,
        closedAt: LocalDateTime,
        endedAt: LocalDate? = null,
        link: String? = null,
    ) {
        this.name = name
        this.status = status
        this.appliedAt = appliedAt
        this.closedAt = closedAt
        this.endedAt = endedAt
        this.link = link
    }
}
