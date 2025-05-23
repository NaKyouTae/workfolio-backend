package com.spectrum.workfolio.domain.entity.history

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

/**
 * 직책
 */
@Entity
@Table(
    name = "position",
    indexes = [
        Index(name = "IDX_POSITION_NAME", columnList = "name"),
        Index(name = "IDX_POSITION_COMPANY_ID", columnList = "company_id")
    ]
)
class Position(
    name: String,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    company: Company,
): BaseEntity("PO") {
    @Column(name = "name", nullable = false)
    var name: String = name
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
