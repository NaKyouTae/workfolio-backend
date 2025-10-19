package com.spectrum.workfolio.domain.entity.interview

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.JobSearchCompanyStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "job_search_companies",
    indexes = [
        Index(name = "idx_job_search_companies_job_search_id", columnList = "job_search_id"),
    ],
)
class JobSearchCompany(
    name: String,
    status: JobSearchCompanyStatus,
    appliedAt: LocalDateTime? = null,
    closedAt: LocalDateTime? = null,
    endedAt: LocalDate? = null,
    industry: String? = null,
    location: String? = null,
    businessSize: String? = null,
    description: String? = null,
    memo: String? = null,
    link: String? = null,
    jobSearch: JobSearch,
) : BaseEntity("JC") {

    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 128, nullable = false)
    var status: JobSearchCompanyStatus = status
        protected set

    @Column(name = "applied_at", nullable = true)
    var appliedAt: LocalDateTime? = appliedAt
        protected set

    @Column(name = "closed_at", nullable = true)
    var closedAt: LocalDateTime? = closedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "location", length = 2048, nullable = true)
    var location: String? = location
        protected set

    @Column(name = "industry", length = 512, nullable = true)
    var industry: String? = industry
        protected set

    @Column(name = "business_size", length = 512, nullable = true)
    var businessSize: String? = businessSize
        protected set

    @Column(name = "memo", columnDefinition = "TEXT", nullable = true)
    var memo: String? = memo
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    var description: String? = description
        protected set

    @Column(name = "link", columnDefinition = "TEXT", nullable = true)
    var link: String? = link
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_search_id", nullable = false)
    var jobSearch: JobSearch = jobSearch
        protected set

    @OneToMany(mappedBy = "jobSearchCompany", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableInterviews: MutableList<Interview> = mutableListOf()
    val interviews: List<Interview> get() = mutableInterviews.toList()

    fun changeInfo(
        name: String,
        status: JobSearchCompanyStatus,
        appliedAt: LocalDateTime? = null,
        closedAt: LocalDateTime? = null,
        endedAt: LocalDate? = null,
        industry: String,
        location: String,
        businessSize: String,
        description: String? = null,
        memo: String? = null,
        link: String? = null,
    ) {
        this.name = name
        this.status = status
        this.appliedAt = appliedAt
        this.closedAt = closedAt
        this.endedAt = endedAt
        this.industry = industry
        this.location = location
        this.businessSize = businessSize
        this.description = description
        this.memo = memo
        this.link = link
    }
}
