package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "payment_tx",
    schema = "payment",
    indexes = [
        Index(name = "idx_payment_tx_payment_id", columnList = "payment_id"),
        Index(name = "idx_payment_tx_target", columnList = "target_type"),
        Index(name = "idx_payment_tx_pg_tx_id", columnList = "pg_tx_id"),
        Index(name = "idx_payment_tx_status", columnList = "status"),
        Index(name = "idx_payment_tx_parent_tx_id", columnList = "parent_tx_id"),
        Index(name = "idx_payment_tx_previous_tx_id", columnList = "previous_tx_id"),
        Index(name = "idx_payment_tx_provider_id", columnList = "provider_id"),
        Index(name = "idx_payment_tx_customer_id", columnList = "customer_id"),
        Index(name = "idx_payment_tx_response_code", columnList = "response_code"),
        Index(name = "idx_payment_tx_created_at", columnList = "created_at"),
        Index(name = "idx_payment_tx_payment_status", columnList = "payment_id, status"),
        Index(name = "idx_payment_tx_parent_status", columnList = "parent_tx_id, status"),
    ],
)
class PaymentTx(
    status: String,
    targetType: String,
    targetIds: String,
    pgType: String,
    amount: BigDecimal,
    paidAmount: BigDecimal = BigDecimal.ZERO,
    pgTxId: String? = null,
    providerId: String? = null,
    parentTxId: String? = null,
    customerId: String? = null,
    receiptUrl: String? = null,
    approveNumber: String? = null,
    paymentMethod: String? = null,
    cardCompany: String? = null,
    cardNumber: String? = null,
    installmentMonth: Int? = null,
    retryCount: Int = 0,
    maxRetryCount: Int = 3,
    previousTxId: String? = null,
    requestData: String? = null,
    responseData: String? = null,
    responseCode: String? = null,
    failedReason: String? = null,
    reason: String? = null,
    metadata: String? = null,
    succeededAt: LocalDateTime? = null,
    failedAt: LocalDateTime? = null,
    paymentId: String,
) : BaseEntity("PT") {

    @Generated(event = [EventType.INSERT])
    @Column(name = "idx", nullable = false, unique = true, insertable = false, updatable = false)
    var idx: Long = 0
        protected set

    @Column(name = "status", length = 32, nullable = false)
    var status: String = status
        protected set

    @Column(name = "target_type", length = 32, nullable = false)
    var targetType: String = targetType
        protected set

    @Column(name = "target_ids", columnDefinition = "JSON", nullable = false)
    var targetIds: String = targetIds
        protected set

    @Column(name = "pg_type", length = 32, nullable = false)
    var pgType: String = pgType
        protected set

    @Column(name = "pg_tx_id", length = 256, nullable = true)
    var pgTxId: String? = pgTxId
        protected set

    @Column(name = "amount", precision = 19, scale = 0, nullable = false)
    var amount: BigDecimal = amount
        protected set

    @Column(name = "paid_amount", precision = 19, scale = 0, nullable = false)
    var paidAmount: BigDecimal = paidAmount
        protected set

    @Column(name = "provider_id", length = 256, nullable = true)
    var providerId: String? = providerId
        protected set

    @Column(name = "parent_tx_id", length = 28, nullable = true)
    var parentTxId: String? = parentTxId
        protected set

    @Column(name = "customer_id", length = 28, nullable = true)
    var customerId: String? = customerId
        protected set

    @Column(name = "receipt_url", columnDefinition = "TEXT", nullable = true)
    var receiptUrl: String? = receiptUrl
        protected set

    @Column(name = "approve_number", length = 512, nullable = true)
    var approveNumber: String? = approveNumber
        protected set

    @Column(name = "payment_method", length = 32, nullable = true)
    var paymentMethod: String? = paymentMethod
        protected set

    @Column(name = "card_company", length = 32, nullable = true)
    var cardCompany: String? = cardCompany
        protected set

    @Column(name = "card_number", length = 64, nullable = true)
    var cardNumber: String? = cardNumber
        protected set

    @Column(name = "installment_month", nullable = true)
    var installmentMonth: Int? = installmentMonth
        protected set

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = retryCount
        protected set

    @Column(name = "max_retry_count", nullable = false)
    var maxRetryCount: Int = maxRetryCount
        protected set

    @Column(name = "previous_tx_id", length = 28, nullable = true)
    var previousTxId: String? = previousTxId
        protected set

    @Column(name = "request_data", columnDefinition = "TEXT", nullable = true)
    var requestData: String? = requestData
        protected set

    @Column(name = "response_data", columnDefinition = "TEXT", nullable = true)
    var responseData: String? = responseData
        protected set

    @Column(name = "response_code", length = 512, nullable = true)
    var responseCode: String? = responseCode
        protected set

    @Column(name = "failed_reason", columnDefinition = "TEXT", nullable = true)
    var failedReason: String? = failedReason
        protected set

    @Column(name = "reason", columnDefinition = "TEXT", nullable = true)
    var reason: String? = reason
        protected set

    @Column(name = "metadata", columnDefinition = "JSON", nullable = true)
    var metadata: String? = metadata
        protected set

    @Column(name = "succeeded_at", nullable = true)
    var succeededAt: LocalDateTime? = succeededAt
        protected set

    @Column(name = "failed_at", nullable = true)
    var failedAt: LocalDateTime? = failedAt
        protected set

    @Column(name = "payment_id", length = 28, nullable = false)
    var paymentId: String = paymentId
        protected set

    fun changeStatus(status: String) {
        this.status = status
    }

    fun updatePaidAmount(paidAmount: BigDecimal) {
        this.paidAmount = paidAmount
    }

    fun incrementRetryCount() {
        this.retryCount++
    }

    fun markAsSucceeded(succeededAt: LocalDateTime = LocalDateTime.now()) {
        this.succeededAt = succeededAt
    }

    fun markAsFailed(failedReason: String?, failedAt: LocalDateTime = LocalDateTime.now()) {
        this.failedReason = failedReason
        this.failedAt = failedAt
    }
}
