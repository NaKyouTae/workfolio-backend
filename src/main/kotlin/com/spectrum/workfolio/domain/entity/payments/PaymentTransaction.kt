package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.TransactionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(
    name = "payment_transactions",
    indexes = [
        Index(name = "idx_payment_transactions_payment_id", columnList = "payment_id"),
        Index(name = "idx_payment_transactions_transaction_type", columnList = "transaction_type"),
        Index(name = "idx_payment_transactions_status", columnList = "status"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class PaymentTransaction(
    payment: Payment,
    transactionType: TransactionType,
    status: String,
    amount: Long,
    transactionId: String? = null,
    requestData: String? = null,
    responseData: String? = null,
    errorMessage: String? = null,
    receiptUrl: String? = null,
) : BaseEntity("PT") {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    var payment: Payment = payment
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 32, nullable = false)
    var transactionType: TransactionType = transactionType
        protected set

    @Column(name = "status", length = 32, nullable = false)
    var status: String = status
        protected set

    @Column(name = "amount", nullable = false)
    var amount: Long = amount
        protected set

    @Column(name = "transaction_id", length = 256, nullable = true)
    var transactionId: String? = transactionId
        protected set

    @Column(name = "request_data", columnDefinition = "jsonb", nullable = true)
    var requestData: String? = requestData
        protected set

    @Column(name = "response_data", columnDefinition = "jsonb", nullable = true)
    var responseData: String? = responseData
        protected set

    @Column(name = "error_message", columnDefinition = "TEXT", nullable = true)
    var errorMessage: String? = errorMessage
        protected set

    @Column(name = "receipt_url", length = 1024, nullable = true)
    var receiptUrl: String? = receiptUrl
        protected set

    fun changeInfo(
        transactionType: TransactionType,
        status: String,
        amount: Long,
        transactionId: String?,
    ) {
        this.transactionType = transactionType
        this.status = status
        this.amount = amount
        this.transactionId = transactionId
    }
}
