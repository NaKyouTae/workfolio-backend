package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
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
    workerId: String,
    amount: BigDecimal,
    status: String = "PENDING",
    paymentMethod: String,
    paymentProvider: String = "",
    providerPaymentId: String = "",
    currency: String = "KRW",
    metadataJson: String? = null,
    creditPlanId: String? = null,
    creditsToAdd: Int = 0,
) : BaseEntity("PA") {

    @Column(name = "worker_id", length = 16, nullable = false)
    var workerId: String = workerId
        protected set

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal = amount
        protected set

    @Column(name = "currency", length = 8, nullable = false)
    var currency: String = currency
        protected set

    @Column(name = "status", length = 32, nullable = false)
    var status: String = status
        protected set

    @Column(name = "payment_method", length = 32, nullable = false)
    var paymentMethod: String = paymentMethod
        protected set

    @Column(name = "payment_provider", length = 64, nullable = false)
    var paymentProvider: String = paymentProvider
        protected set

    @Column(name = "provider_payment_id", length = 256, nullable = false, unique = true)
    var providerPaymentId: String = providerPaymentId
        protected set

    @Column(name = "paid_at", nullable = true)
    var paidAt: LocalDateTime? = null
        protected set

    @Column(name = "refunded_at", nullable = true)
    var refundedAt: LocalDateTime? = null
        protected set

    @Column(name = "refund_amount", nullable = false)
    var refundAmount: BigDecimal = BigDecimal.ZERO
        protected set

    @Column(name = "refund_reason", length = 512, nullable = true)
    var refundReason: String? = null
        protected set

    @Column(name = "failure_reason", length = 512, nullable = true)
    var failureReason: String? = null
        protected set

    @Column(name = "failure_code", length = 64, nullable = true)
    var failureCode: String? = null
        protected set

    @Column(name = "metadata_json", columnDefinition = "JSONB", nullable = true)
    var metadataJson: String? = metadataJson
        protected set

    @Column(name = "credit_plan_id", length = 16, nullable = true)
    var creditPlanId: String? = creditPlanId
        protected set

    @Column(name = "credits_to_add", nullable = false)
    var creditsToAdd: Int = creditsToAdd
        protected set

    fun confirm(providerPaymentId: String, paymentProvider: String) {
        this.status = "COMPLETED"
        this.providerPaymentId = providerPaymentId
        this.paymentProvider = paymentProvider
        this.paidAt = LocalDateTime.now()
    }

    fun fail(failureReason: String?, failureCode: String? = null) {
        this.status = "FAILED"
        this.failureReason = failureReason
        this.failureCode = failureCode
    }

    fun refund(refundAmount: BigDecimal, refundReason: String?) {
        this.status = "REFUNDED"
        this.refundAmount = refundAmount
        this.refundReason = refundReason
        this.refundedAt = LocalDateTime.now()
    }
}
