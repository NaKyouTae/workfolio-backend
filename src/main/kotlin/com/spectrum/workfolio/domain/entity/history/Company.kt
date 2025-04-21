package com.spectrum.workfolio.domain.entity.history

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 회사
 */
@Entity
@Table(
    name = "company",
    indexes = [
        Index(name = "IDX_COMPANY_NAME", columnList = "name"),
        Index(name = "IDX_COMPANY_WORKER_ID", columnList = "worker_id")
    ]
)
class Company(
    name: String,
    startedAt: LocalDateTime,
    endedAt: LocalDateTime? = null,
    isWorking: Boolean = false,
    worker: Worker,
) : BaseEntity("CO") {
    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDateTime? = endedAt
        protected set

    @Column(name = "is_working", nullable = false)
    var isWorking: Boolean = isWorking
    protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set
}
