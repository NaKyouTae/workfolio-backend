package com.spectrum.workfolio.domain.entity.history

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

/**
 * 연봉
 */
@Entity
@Table(
    name = "salary",
    indexes = [
        Index(name = "idx_salary_amount", columnList = "amount"),
        Index(name = "idx_salary_company_id", columnList = "company_id")
    ]
)
class Salary(
    amount: Long = 0,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    company: Company,
) : BaseEntity("SA") {
    @Column(name = "amount", nullable = false)
    var amount: Long = amount
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company = company
}
