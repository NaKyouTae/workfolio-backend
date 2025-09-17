package com.spectrum.workfolio.domain.entity.primary

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.*
import java.time.LocalDate

/**
 * 자격증
 */
@Entity
@Table(
    name = "certifications",
    indexes = [
        Index(name = "idx_certifications_name", columnList = "name"),
        Index(name = "idx_certifications_worker_id", columnList = "worker_id")
    ]
)
class Certifications(
    name: String,
    number: String,
    issuer: String,
    issuedAt: LocalDate,
    expirationPeriod: LocalDate,
    worker: Worker,
) : BaseEntity("CE") {
    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Column(name = "number", nullable = false)
    var number: String = number
        protected set

    @Column(name = "issuer", nullable = false)
    var issuer: String = issuer
        protected set

    @Column(name = "issued_at", nullable = false)
    var issuedAt: LocalDate = issuedAt
        protected set

    @Column(name = "expiration_period", nullable = false)
    var expirationPeriod: LocalDate = expirationPeriod
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set
}
