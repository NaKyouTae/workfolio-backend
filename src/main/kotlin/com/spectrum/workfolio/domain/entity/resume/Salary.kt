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
        Index(name = "idx_salaries_career_id", columnList = "career_id"),
    ],
)
class Salary(
    amount: Long = 0,
    negotiationDate: LocalDate? = null,
    memo: String? = null,
    isVisible: Boolean = false,
    career: Career,
) : BaseEntity("SA") {
    @Column(name = "amount", nullable = false)
    var amount: Long = amount
        protected set

    @Column(name = "negotiation_date", nullable = true)
    var negotiationDate: LocalDate? = negotiationDate
        protected set

    @Column(name = "memo", nullable = true)
    var memo: String? = memo
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    var career: Career = career

    fun changeInfo(amount: Long, negotiationDate: LocalDate?, memo: String?, isVisible: Boolean) {
        this.amount = amount
        this.negotiationDate = negotiationDate
        this.memo = memo
        this.isVisible = isVisible
    }
}
