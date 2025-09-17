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
        Index(name = "idx_company_name", columnList = "name"),
        Index(name = "idx_company_worker_id", columnList = "worker_id")
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

    @OneToMany(mappedBy = "company", cascade = [CascadeType.REMOVE])
    private var mutablePositions: MutableList<Position> = mutableListOf()
    val positions: List<Position> get() = mutablePositions.toList()

    @OneToMany(mappedBy = "company", cascade = [CascadeType.REMOVE])
    private var mutableSalaries: MutableList<Salary> = mutableListOf()
    val salaries: List<Salary> get() = mutableSalaries.toList()
}
