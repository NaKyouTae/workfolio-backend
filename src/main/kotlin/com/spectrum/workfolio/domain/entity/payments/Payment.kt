package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.math.BigDecimal

@Entity
@Table(
    name = "payments",
    schema = "payment",
    indexes = [
        Index(name = "idx_payments_order_id", columnList = "order_id"),
        Index(name = "idx_payments_status", columnList = "status, created_at"),
        Index(name = "idx_payments_created_at", columnList = "created_at"),
        Index(name = "idx_payments_order_status", columnList = "order_id, status, created_at"),
    ],
)
class Payment(
    status: String,
    type: String,
    totalAmount: BigDecimal,
    paidAmount: BigDecimal = BigDecimal.ZERO,
    currency: String = "KRW",
    metadata: String? = null,
    orderId: String,
) : BaseEntity("PA") {

    @Generated(event = [EventType.INSERT])
    @Column(name = "idx", nullable = false, unique = true, insertable = false, updatable = false)
    var idx: Long = 0
        protected set

    @Column(name = "status", length = 32, nullable = false)
    var status: String = status
        protected set

    @Column(name = "type", length = 32, nullable = false)
    var type: String = type
        protected set

    @Column(name = "total_amount", precision = 19, scale = 0, nullable = false)
    var totalAmount: BigDecimal = totalAmount
        protected set

    @Column(name = "paid_amount", precision = 19, scale = 0, nullable = false)
    var paidAmount: BigDecimal = paidAmount
        protected set

    @Column(name = "currency", length = 8, nullable = false)
    var currency: String = currency
        protected set

    @Column(name = "metadata", columnDefinition = "JSON", nullable = true)
    var metadata: String? = metadata
        protected set

    @Column(name = "order_id", length = 28, nullable = false)
    var orderId: String = orderId
        protected set

    fun changeStatus(status: String) {
        this.status = status
    }

    fun updatePaidAmount(paidAmount: BigDecimal) {
        this.paidAmount = paidAmount
    }
}
