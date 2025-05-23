package com.spectrum.workfolio.domain.entity.primary

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.*
import java.time.LocalDate

/**
 * 교육
 */
@Entity
@Table(
    name = "educations",
    indexes = [
        Index(name = "IDX_EDUCATIONS_NAME", columnList = "name"),
        Index(name = "IDX_EDUCATIONS_WORKER_ID", columnList = "worker_id")
    ]
)
class Education(
    name: String,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    agency: String,
    worker: Worker,
): BaseEntity("ED") {
    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
    protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
    protected set

    @Column(name = "agency", nullable = false)
    var agency: String = agency
    protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set
}
