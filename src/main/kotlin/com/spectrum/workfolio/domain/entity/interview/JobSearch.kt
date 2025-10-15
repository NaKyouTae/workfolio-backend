package com.spectrum.workfolio.domain.entity.interview

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.history.Company
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "job_search",
    indexes = [
        Index(name = "idx_job_search_worker_id_started_at_ended_at", columnList = "worker_id, started_at, ended_at"),
    ],
)
class JobSearch(
    title: String? = null,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    prevCompany: Company? = null,
    nextCompany: Company? = null,
    memo: String? = null,
    worker: Worker,
) : BaseEntity("JS") {
    @Column(name = "title", length = 1024, nullable = true)
    var title: String? = title
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "memo", columnDefinition = "TEXT", nullable = true)
    var memo: String? = memo
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prev_company_id", nullable = true)
    var prevCompany: Company? = prevCompany
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_company_id", nullable = true)
    var nextCompany: Company? = nextCompany
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @OneToMany(mappedBy = "jobSearch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableJobSearchCompanies: MutableList<JobSearchCompany> = mutableListOf()
    val jobSearchCompanies: List<JobSearchCompany> get() = mutableJobSearchCompanies.toList()

    fun changeInfo(
        title: String?,
        memo: String?,
        startedAt: LocalDate,
        endedAt: LocalDate?,
        prevCompany: Company? = null,
        nextCompany: Company? = null,
    ) {
        this.title = title
        this.memo = memo
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.prevCompany = prevCompany
        this.nextCompany = nextCompany
    }
}
