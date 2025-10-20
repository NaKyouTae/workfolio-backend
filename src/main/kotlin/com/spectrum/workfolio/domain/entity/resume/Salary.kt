package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 연봉
 */
@Entity
@Table(
    name = "salaries",
    indexes = [
        Index(name = "idx_salaries_amount", columnList = "amount"),
        Index(name = "idx_salaries_career_id", columnList = "career_id"),
    ],
)
class Salary(
    amount: Long = 0,
    isVisible: Boolean = false,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    career: Career,
) : BaseEntity("SA") {
    @Column(name = "amount", nullable = false)
    var amount: Long = amount
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    var career: Career = career

    fun changeInfo(amount: Long, startedAt: LocalDate, endedAt: LocalDate?) {
        this.amount = amount
        this.startedAt = startedAt
        this.endedAt = endedAt
    }
}
