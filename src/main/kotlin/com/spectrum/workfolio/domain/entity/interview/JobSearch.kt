package com.spectrum.workfolio.domain.entity.interview

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.resume.Career
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
    name = "job_searches",
    indexes = [
        Index(name = "idx_job_searches_worker_id_started_at_ended_at", columnList = "worker_id, started_at, ended_at"),
    ],
)
class JobSearch(
    title: String? = null,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    prevCareer: Career? = null,
    nextCareer: Career? = null,
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
    @JoinColumn(name = "prev_career_id", nullable = true)
    var prevCareer: Career? = prevCareer
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_career_id", nullable = true)
    var nextCareer: Career? = nextCareer
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
        prevCareer: Career? = null,
        nextCareer: Career? = null,
    ) {
        this.title = title
        this.memo = memo
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.prevCareer = prevCareer
        this.nextCareer = nextCareer
    }
}
