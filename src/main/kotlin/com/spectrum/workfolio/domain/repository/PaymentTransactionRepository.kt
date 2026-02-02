package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.payments.PaymentTransaction
import com.spectrum.workfolio.domain.enums.TransactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentTransactionRepository : JpaRepository<PaymentTransaction, String> {
    fun findByPaymentIdOrderByCreatedAtDesc(paymentId: String): List<PaymentTransaction>
    fun findByPaymentIdAndTransactionType(paymentId: String, transactionType: TransactionType): List<PaymentTransaction>
}
