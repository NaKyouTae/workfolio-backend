package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.Currency
import com.spectrum.workfolio.domain.enums.PaymentMethod
import com.spectrum.workfolio.domain.enums.PaymentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payments_worker_id", columnList = "worker_id"),
        Index(name = "idx_payments_status", columnList = "status"),
        Index(name = "idx_payments_provider_payment_id", columnList = "provider_payment_id"),
    ],
)
class Payment(
    amount: Long,
    currency: Currency,
    status: PaymentStatus,
    paymentMethod: PaymentMethod,
    paymentProvider: String,
    providerPaymentId: String,
    paidAt: LocalDateTime? = null,
    refundedAt: LocalDateTime? = null,
    refundAmount: Long = 0,
    refundReason: String? = null,
    failureReason: String? = null,
    failureCode: String? = null,
    metadataJson: String? = null,
    worker: Worker,
) : BaseEntity("PA") {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @Column(name = "amount", nullable = false)
    var amount: Long = amount
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 8, nullable = false)
    var currency: Currency = currency
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: PaymentStatus = status
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 32, nullable = false)
    var paymentMethod: PaymentMethod = paymentMethod
        protected set

    @Column(name = "payment_provider", length = 64, nullable = false)
    var paymentProvider: String = paymentProvider
        protected set

    @Column(name = "provider_payment_id", length = 256, nullable = false, unique = true)
    var providerPaymentId: String = providerPaymentId
        protected set

    @Column(name = "paid_at", nullable = true)
    var paidAt: LocalDateTime? = paidAt
        protected set

    @Column(name = "refunded_at", nullable = true)
    var refundedAt: LocalDateTime? = refundedAt
        protected set

    @Column(name = "refund_amount", nullable = false)
    var refundAmount: Long = refundAmount
        protected set

    @Column(name = "refund_reason", length = 512, nullable = true)
    var refundReason: String? = refundReason
        protected set

    @Column(name = "failure_reason", length = 512, nullable = true)
    var failureReason: String? = failureReason
        protected set

    @Column(name = "failure_code", length = 64, nullable = true)
    var failureCode: String? = failureCode
        protected set

    @Column(name = "metadata_json", columnDefinition = "jsonb", nullable = true)
    var metadataJson: String? = metadataJson
        protected set

    @Column(name = "credit_plan_id", length = 16, nullable = true)
    var creditPlanId: String? = null
        protected set

    @Column(name = "credits_to_add", nullable = false)
    var creditsToAdd: Int = 0
        protected set

    fun changeInfo(
        amount: Long,
        currency: Currency,
        status: PaymentStatus,
        paymentMethod: PaymentMethod,
        paymentProvider: String,
        providerPaymentId: String,
    ) {
        this.amount = amount
        this.currency = currency
        this.status = status
        this.paymentMethod = paymentMethod
        this.paymentProvider = paymentProvider
        this.providerPaymentId = providerPaymentId
    }

    fun setCreditPlanInfo(creditPlanId: String, creditsToAdd: Int) {
        this.creditPlanId = creditPlanId
        this.creditsToAdd = creditsToAdd
    }

    fun changeStatus(status: PaymentStatus) {
        this.status = status
    }

    fun markAsPaid(paidAt: LocalDateTime = LocalDateTime.now()) {
        this.status = PaymentStatus.COMPLETED
        this.paidAt = paidAt
    }

    fun markAsRefunded(refundAmount: Long, refundReason: String?, refundedAt: LocalDateTime = LocalDateTime.now()) {
        this.status = PaymentStatus.REFUNDED
        this.refundAmount = refundAmount
        this.refundReason = refundReason
        this.refundedAt = refundedAt
    }
}
