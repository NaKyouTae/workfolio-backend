package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.ApplicationStageStatus
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

@Entity
@Table(
    name = "application_stages",
    indexes = [
        Index(name = "idx_application_stages_job_application_id", columnList = "job_application_id"),
    ],
)
class ApplicationStage(
    name: String, // 절차 명
    status: ApplicationStageStatus, // 진행 상태
    startedAt: LocalDate? = null, // 진행 일자
    memo: String, // 메모
    jobApplication: JobApplication,
    isVisible: Boolean = true,
    priority: Int = 0,
) : BaseEntity("JA") {
    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: ApplicationStageStatus = status
        protected set

    @Column(name = "started_at", nullable = true)
    var startedAt: LocalDate? = startedAt
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
    @JoinColumn(name = "job_application_id", nullable = false)
    var jobApplication: JobApplication = jobApplication
        protected set

    fun changeInfo(
        name: String, // 절차 명
        status: ApplicationStageStatus, // 진행 상태
        startedAt: LocalDate? = null, // 진행 일자
        memo: String, // 메모
        isVisible: Boolean,
        priority: Int,
    ) {
        this.name = name
        this.status = status
        this.startedAt = startedAt
        this.memo = memo
        this.isVisible = isVisible
        this.priority = priority
    }
}
