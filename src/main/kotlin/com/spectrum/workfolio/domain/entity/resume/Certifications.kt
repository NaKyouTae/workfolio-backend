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
 * 자격증
 */
@Entity
@Table(
    name = "certifications",
    indexes = [
        Index(name = "idx_certifications_name", columnList = "name"),
        Index(name = "idx_certifications_resume_id", columnList = "resume_id"),
    ],
)
class Certifications(
    name: String,
    number: String,
    issuer: String,
    issuedAt: LocalDate,
    expirationPeriod: LocalDate? = null,
    isVisible: Boolean = false,
    resume: Resume,
) : BaseEntity("CE") {
    @Column(name = "name", nullable = false, unique = true)
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

    @Column(name = "expiration_period", nullable = true)
    var expirationPeriod: LocalDate? = expirationPeriod
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    fun changeInfo(name: String, number: String, issuer: String, issuedAt: LocalDate, expirationPeriod: LocalDate?) {
        this.name = name
        this.number = number
        this.issuer = issuer
        this.issuedAt = issuedAt
        this.expirationPeriod = expirationPeriod
    }
}
