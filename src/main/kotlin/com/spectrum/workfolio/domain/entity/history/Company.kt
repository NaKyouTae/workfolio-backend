package com.spectrum.workfolio.domain.entity.history

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
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

/**
 * 회사
 */
@Entity
@Table(
    name = "company",
    indexes = [
        Index(name = "idx_company_name", columnList = "name"),
        Index(name = "idx_company_worker_id", columnList = "worker_id"),
    ],
)
class Company(
    name: String,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    isWorking: Boolean = false,
    worker: Worker,
) : BaseEntity("CO") {
    @Column(name = "name", nullable = false, unique = true)
    var name: String = name
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "is_working", nullable = false)
    var isWorking: Boolean = isWorking
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @OneToMany(mappedBy = "company", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutablePositions: MutableList<Position> = mutableListOf()
    val positions: List<Position> get() = mutablePositions.toList()

    @OneToMany(mappedBy = "company", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableSalaries: MutableList<Salary> = mutableListOf()
    val salaries: List<Salary> get() = mutableSalaries.toList()

    fun addPosition(position: Position) {
        mutablePositions.add(position)
    }

    fun addSalary(salary: Salary) {
        mutableSalaries.add(salary)
    }

    fun changeInfo(name: String, startedAt: LocalDate, endedAt: LocalDate?, isWorking: Boolean) {
        this.name = name
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.isWorking = isWorking
    }
}
