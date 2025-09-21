package com.spectrum.workfolio.domain.entity.interview

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.InterviewType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "interview",
    indexes = [
        Index(name = "idx_interview_job_search_company_id", columnList = "job_search_company_id"),
    ],
)
class Interview(
    title: String? = null,
    type: InterviewType,
    startedAt: LocalDateTime? = null,
    endedAt: LocalDateTime? = null,
    memo: String? = null,
    jobSearchCompany: JobSearchCompany,
) : BaseEntity("IV") {

    @Column(name = "title", length = 1024, nullable = true)
    var title: String? = title
        protected set

    @Column(name = "type", length = 128, nullable = false)
    var type: InterviewType = type
        protected set

    @Column(name = "started_at", nullable = true)
    var startedAt: LocalDateTime? = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDateTime? = endedAt
        protected set

    @Column(name = "memo", columnDefinition = "TEXT", nullable = true)
    var memo: String? = memo
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_search_company_id", nullable = false)
    var jobSearchCompany: JobSearchCompany = jobSearchCompany
        protected set

    fun changeInfo(
        title: String? = null,
        type: InterviewType,
        startedAt: LocalDateTime? = null,
        endedAt: LocalDateTime? = null,
        memo: String? = null,
    ) {
        this.title = title
        this.type = type
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.memo = memo
    }
}
