package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.payments.Payment
import com.spectrum.workfolio.domain.entity.payments.PaymentTx
import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.domain.repository.PaymentRepository
import com.spectrum.workfolio.domain.repository.PaymentTxRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentTxRepository: PaymentTxRepository,
    private val creditPlanService: CreditPlanService,
    private val creditService: CreditService,
) {

    fun getPaymentById(id: String): Payment {
        return paymentRepository.findById(id)
            .orElseThrow { WorkfolioException("결제 정보를 찾을 수 없습니다.") }
    }

    @Transactional(readOnly = true)
    fun getPaymentsByWorkerId(workerId: String, pageable: Pageable): Page<Payment> {
        return paymentRepository.findByWorkerIdOrderByCreatedAtDesc(workerId, pageable)
    }

    @Transactional(readOnly = true)
    fun getPaymentsByStatus(status: String, pageable: Pageable): Page<Payment> {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
    }

    @Transactional(readOnly = true)
    fun getPaymentTransactions(paymentId: String): List<PaymentTx> {
        return paymentTxRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
    }

    @Transactional
    fun createPayment(workerId: String, creditPlanId: String, paymentMethod: String): Payment {
        val creditPlan = creditPlanService.getActiveCreditPlanById(creditPlanId)

        val payment = Payment(
            workerId = workerId,
            amount = BigDecimal.valueOf(creditPlan.price.toLong()),
            paymentMethod = paymentMethod,
            creditPlanId = creditPlanId,
            creditsToAdd = creditPlan.totalCredits,
            metadataJson = """{"creditPlanName":"${creditPlan.name}","baseCredits":${creditPlan.baseCredits},"bonusCredits":${creditPlan.bonusCredits}}""",
        )
        val savedPayment = paymentRepository.save(payment)

        val tx = PaymentTx(
            paymentId = savedPayment.id,
            transactionType = "PAYMENT",
            status = "PENDING",
            amount = savedPayment.amount,
        )
        paymentTxRepository.save(tx)

        return savedPayment
    }

    @Transactional
    fun confirmPayment(paymentId: String, providerPaymentId: String): Payment {
        val payment = getPaymentById(paymentId)

        if (payment.status != "PENDING") {
            throw WorkfolioException("이미 처리된 결제입니다.")
        }

        payment.confirm(providerPaymentId, "TOSS")

        val txList = paymentTxRepository.findByPaymentIdAndStatus(paymentId, "PENDING")
        txList.forEach { tx ->
            tx.changeStatus("COMPLETED")
            tx.updateTransactionId(providerPaymentId)
        }

        paymentRepository.save(payment)

        // 크레딧 추가 → CreditHistory 자동 생성
        val baseCredits = payment.creditsToAdd
        if (baseCredits > 0) {
            creditService.addCredits(
                workerId = payment.workerId,
                amount = baseCredits,
                txType = CreditTxType.CHARGE,
                referenceType = "CREDIT_PLAN",
                referenceId = payment.creditPlanId,
                description = "크레딧 충전",
            )
        }

        return payment
    }

    @Transactional
    fun deletePayment(id: String) {
        val payment = getPaymentById(id)
        paymentRepository.delete(payment)
    }
}
