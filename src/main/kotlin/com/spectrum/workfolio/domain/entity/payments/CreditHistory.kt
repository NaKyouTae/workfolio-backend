package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.utils.StringUtil
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

@Entity
@Table(
    name = "credit_histories",
    indexes = [
        Index(name = "idx_credit_histories_worker_created", columnList = "worker_id, created_at"),
        Index(name = "idx_credit_histories_tx_type", columnList = "tx_type"),
    ],
)
class CreditHistory(
    worker: Worker,
    txType: CreditTxType,
    amount: Int,
    balanceBefore: Int,
    balanceAfter: Int,
    referenceType: String? = null,
    referenceId: String? = null,
    description: String? = null,
    metadata: String? = null,
) : Persistable<String> {

    @Id
    @Column(length = 16, nullable = false, updatable = false)
    private val id: String = StringUtil.generateUUID("CH")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", length = 20, nullable = false)
    var txType: CreditTxType = txType
        protected set

    @Column(name = "amount", nullable = false)
    var amount: Int = amount
        protected set

    @Column(name = "balance_before", nullable = false)
    var balanceBefore: Int = balanceBefore
        protected set

    @Column(name = "balance_after", nullable = false)
    var balanceAfter: Int = balanceAfter
        protected set

    @Column(name = "reference_type", length = 30, nullable = true)
    var referenceType: String? = referenceType
        protected set

    @Column(name = "reference_id", length = 64, nullable = true)
    var referenceId: String? = referenceId
        protected set

    @Column(name = "description", length = 500, nullable = true)
    var description: String? = description
        protected set

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb", nullable = true)
    var metadata: String? = metadata
        protected set

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    override fun getId(): String = id

    override fun isNew(): Boolean = true
}
