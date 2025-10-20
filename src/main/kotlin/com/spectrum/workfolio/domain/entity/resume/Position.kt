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
 * 직책
 */
@Entity
@Table(
    name = "positions",
    indexes = [
        Index(name = "idx_positions_name", columnList = "name"),
        Index(name = "idx_positions_career_id", columnList = "career_id"),
    ],
)
class Position(
    name: String,
    isVisible: Boolean = false,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    career: Career,
) : BaseEntity("PO") {
    @Column(name = "name", nullable = false, unique = true)
    var name: String = name
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

    fun changeInfo(name: String, startedAt: LocalDate, endedAt: LocalDate?) {
        this.name = name
        this.startedAt = startedAt
        this.endedAt = endedAt
    }
}
