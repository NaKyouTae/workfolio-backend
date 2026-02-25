package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.utils.StringUtil
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.Persistable
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "payment_tx",
    indexes = [
        Index(name = "idx_payment_tx_payment_id", columnList = "payment_id"),
        Index(name = "idx_payment_tx_transaction_type", columnList = "transaction_type"),
        Index(name = "idx_payment_tx_status", columnList = "status"),
    ],
)
class PaymentTx(
    paymentId: String,
    transactionType: String,
    status: String,
    amount: BigDecimal,
    transactionId: String? = null,
    requestData: String? = null,
    responseData: String? = null,
    errorMessage: String? = null,
    receiptUrl: String? = null,
) : Persistable<String> {

    @Id
    @Column(length = 16, nullable = false, updatable = false)
    private val id: String = StringUtil.generateUUID("PT")

    @Column(name = "payment_id", length = 16, nullable = false)
    var paymentId: String = paymentId
        protected set

    @Column(name = "transaction_type", length = 32, nullable = false)
    var transactionType: String = transactionType
        protected set

    @Column(name = "status", length = 32, nullable = false)
    var status: String = status
        protected set

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal = amount
        protected set

    @Column(name = "transaction_id", length = 256, nullable = true)
    var transactionId: String? = transactionId
        protected set

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_data", columnDefinition = "JSONB", nullable = true)
    var requestData: String? = requestData
        protected set

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_data", columnDefinition = "JSONB", nullable = true)
    var responseData: String? = responseData
        protected set

    @Column(name = "error_message", columnDefinition = "TEXT", nullable = true)
    var errorMessage: String? = errorMessage
        protected set

    @Column(name = "receipt_url", length = 1024, nullable = true)
    var receiptUrl: String? = receiptUrl
        protected set

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    override fun getId(): String = id

    override fun isNew(): Boolean = true

    fun changeStatus(status: String) {
        this.status = status
    }

    fun updateTransactionId(transactionId: String) {
        this.transactionId = transactionId
    }

    fun updateResponseData(responseData: String?) {
        this.responseData = responseData
    }
}
