package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.payments.PaymentTx
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentTxRepository : JpaRepository<PaymentTx, String> {
    fun findByPaymentIdOrderByCreatedAtDesc(paymentId: String): List<PaymentTx>
    fun findByPaymentIdAndStatus(paymentId: String, status: String): List<PaymentTx>
    fun findByTransactionId(transactionId: String): PaymentTx?
}
