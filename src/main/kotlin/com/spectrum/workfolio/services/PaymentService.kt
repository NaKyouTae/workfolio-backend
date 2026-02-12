package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.payments.Payment
import com.spectrum.workfolio.domain.entity.payments.PaymentTx
import com.spectrum.workfolio.domain.repository.PaymentRepository
import com.spectrum.workfolio.domain.repository.PaymentTxRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentTxRepository: PaymentTxRepository,
) {

    fun getPaymentById(id: String): Payment {
        return paymentRepository.findById(id)
            .orElseThrow { WorkfolioException("결제 정보를 찾을 수 없습니다.") }
    }

    @Transactional(readOnly = true)
    fun getPaymentsByOrderId(orderId: String, pageable: Pageable): Page<Payment> {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId, pageable)
    }

    @Transactional(readOnly = true)
    fun getPaymentsByStatus(status: String, pageable: Pageable): Page<Payment> {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
    }

    @Transactional(readOnly = true)
    fun getPaymentTransactions(paymentId: String): List<PaymentTx> {
        return paymentTxRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
    }

    @Transactional(readOnly = true)
    fun getPaymentTxByPgTxId(pgTxId: String): PaymentTx? {
        return paymentTxRepository.findByPgTxId(pgTxId)
    }
}
