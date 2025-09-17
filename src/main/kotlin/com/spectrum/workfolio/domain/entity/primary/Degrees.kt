package com.spectrum.workfolio.domain.entity.primary

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.*
import java.time.LocalDate

/**
 * 학위
 */
@Entity
@Table(
    name = "degrees",
    indexes = [
        Index(name = "idx_degrees_name", columnList = "name"),
        Index(name = "idx_degrees_worker_id", columnList = "worker_id")
    ]
)
class Degrees(
    name: String,
    major: String,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    worker: Worker,
) : BaseEntity("DE") {

    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Column(name = "major", nullable = false)
    var major: String = major
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set
}
