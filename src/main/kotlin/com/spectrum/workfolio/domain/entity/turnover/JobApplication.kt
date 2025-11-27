package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.JobApplicationStatus
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

@Entity
@Table(
    name = "job_applications",
    indexes = [
        Index(name = "idx_job_applications_turn_over_id", columnList = "turn_over_id"),
    ],
)
class JobApplication(
    name: String, // 회사명
    status: JobApplicationStatus, // 지원 상태
    position: String, // 직무
    jobPostingTitle: String, // 공고명
    jobPostingUrl: String, // 공고문 URL
    startedAt: LocalDate? = null, // 모집 시작 기간
    endedAt: LocalDate? = null, // 모집 마감 기간
    applicationSource: String, // 지원 경로
    memo: String, // 메모
    turnOver: TurnOver,
    isVisible: Boolean = true,
    priority: Int = 0,
) : BaseEntity("JA") {
    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: JobApplicationStatus = status
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

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_id", nullable = false)
    var turnOver: TurnOver = turnOver
        protected set

    @OneToMany(
        mappedBy = "jobApplication",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true,
    )
    private var mutableApplicationStages: MutableList<ApplicationStage> = mutableListOf()
    val applicationStages: List<ApplicationStage> get() = mutableApplicationStages.toList()

    // Cascade를 위한 컬렉션 동기화 메서드
    fun syncApplicationStages(newApplicationStages: List<ApplicationStage>) {
        mutableApplicationStages.clear()
        mutableApplicationStages.addAll(newApplicationStages)
    }

    fun changeInfo(
        name: String, // 회사명
        status: JobApplicationStatus,
        position: String, // 직무
        jobPostingTitle: String, // 공고명
        jobPostingUrl: String, // 공고문 URL
        startedAt: LocalDate? = null, // 모집 시작 기간
        endedAt: LocalDate? = null, // 모집 마감 기간
        applicationSource: String, // 지원 경로
        memo: String, // 메모
        isVisible: Boolean,
        priority: Int,
    ) {
        this.name = name
        this.status = status
        this.position = position
        this.jobPostingTitle = jobPostingTitle
        this.jobPostingUrl = jobPostingUrl
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.applicationSource = applicationSource
        this.memo = memo
        this.isVisible = isVisible
        this.priority = priority
    }
}
