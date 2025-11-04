package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
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
    name = "job_applications",
    indexes = [
        Index(name = "idx_job_applications_turn_over_challenge_id", columnList = "turn_over_challenge_id"),
    ],
)
class JobApplication(
    name: String, // 회사명
    position: String, // 직무
    jobPostingTitle: String, // 공고명
    jobPostingUrl: String, // 공고문 URL
    startedAt: LocalDate? = null, // 모집 시작 기간
    endedAt: LocalDate? = null, // 모집 마감 기간
    applicationSource: String, // 지원 경로
    memo: String, // 메모
    turnOverChallenge: TurnOverChallenge,
) : BaseEntity("JA") {
    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "position", length = 512, nullable = false)
    var position: String = position
        protected set

    @Column(name = "job_posting_title", length = 1024, nullable = false)
    var jobPostingTitle: String = jobPostingTitle
        protected set

    @Column(name = "job_posting_url", columnDefinition = "TEXT", nullable = false)
    var jobPostingUrl: String = jobPostingUrl
        protected set

    @Column(name = "started_at", nullable = true)
    var startedAt: LocalDate? = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "application_source", length = 1024, nullable = false)
    var applicationSource: String = applicationSource
        protected set

    @Column(name = "memo", columnDefinition = "TEXT", nullable = false)
    var memo: String = memo
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_challenge_id", nullable = false)
    var turnOverChallenge: TurnOverChallenge = turnOverChallenge
        protected set

    @OneToMany(mappedBy = "jobApplication", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableApplicationStages: MutableList<ApplicationStage> = mutableListOf()
    val applicationStages: List<ApplicationStage> get() = mutableApplicationStages.toList()

    fun changeInfo(
        name: String, // 회사명
        position: String, // 직무
        jobPostingTitle: String, // 공고명
        jobPostingUrl: String, // 공고문 URL
        startedAt: LocalDate? = null, // 모집 시작 기간
        endedAt: LocalDate? = null, // 모집 마감 기간
        applicationSource: String, // 지원 경로
        memo: String, // 메모
    ) {
        this.name = name
        this.position = position
        this.jobPostingTitle = jobPostingTitle
        this.jobPostingUrl = jobPostingUrl
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.applicationSource = applicationSource
        this.memo = memo
    }
}
