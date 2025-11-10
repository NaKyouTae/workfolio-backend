package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.domain.enums.PaymentMethod
import com.spectrum.workfolio.domain.enums.PaymentStatus
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency

@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payments_worker_id", columnList = "worker_id, created_at"),
        Index(name = "idx_payments_provider_id", columnList = "provider_id, created_at")
    ],
)
class Payment(
    method: PaymentMethod,
    amount: BigDecimal,
    currency: Currency,
    status: PaymentStatus,
    paidAt: LocalDateTime,
    refundedAt: LocalDateTime,
    refundReason: String,
    refundAmount: BigDecimal,
    providerId: String,
    failureReason: String,
    failureCode: String,
    metadataJson: String,
    worker: Worker,
) : BaseEntity("PA") {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set
}