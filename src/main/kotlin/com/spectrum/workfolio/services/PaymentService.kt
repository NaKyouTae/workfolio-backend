package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.payments.Payment
import com.spectrum.workfolio.domain.entity.payments.PaymentTransaction
import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.domain.enums.Currency
import com.spectrum.workfolio.domain.enums.PaymentMethod
import com.spectrum.workfolio.domain.enums.PaymentStatus
import com.spectrum.workfolio.domain.enums.TransactionType
import com.spectrum.workfolio.domain.repository.PaymentRepository
import com.spectrum.workfolio.domain.repository.PaymentTransactionRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentTransactionRepository: PaymentTransactionRepository,
    private val workerRepository: WorkerRepository,
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

    @Transactional
    fun createPaymentForCreditPlan(
        workerId: String,
        creditPlanId: String,
        paymentMethod: PaymentMethod = PaymentMethod.CARD,
    ): Payment {
        val worker = workerRepository.findById(workerId)
            .orElseThrow { WorkfolioException("사용자를 찾을 수 없습니다.") }

        val creditPlan = creditPlanService.getActiveCreditPlanById(creditPlanId)

        val payment = Payment(
            amount = creditPlan.price.toLong(),
            currency = Currency.KRW,
            status = PaymentStatus.PENDING,
            paymentMethod = paymentMethod,
            paymentProvider = "TOSS",
            providerPaymentId = "",
            worker = worker,
        )
        payment.setCreditPlanInfo(creditPlan.id, creditPlan.totalCredits)

        return paymentRepository.save(payment)
    }

    @Transactional
    fun confirmPayment(
        paymentId: String,
        providerPaymentId: String,
        transactionId: String?,
        responseData: String?,
        receiptUrl: String?,
    ): Payment {
        val payment = getPaymentById(paymentId)

        if (payment.status != PaymentStatus.PENDING) {
            throw WorkfolioException("이미 처리된 결제입니다.")
        }

        payment.markAsPaid(LocalDateTime.now())

        val transaction = PaymentTransaction(
            paymentId = payment.id,
            transactionType = TransactionType.PAYMENT,
            status = "DONE",
            amount = payment.amount,
            transactionId = transactionId,
            responseData = responseData,
            receiptUrl = receiptUrl,
        )
        paymentTransactionRepository.save(transaction)

        if (payment.creditsToAdd > 0) {
            val creditPlanName = payment.creditPlanId?.let { creditPlanService.getCreditPlanById(it).name }
            creditService.addCredits(
                workerId = payment.worker.id,
                amount = payment.creditsToAdd,
                txType = CreditTxType.CHARGE,
                referenceType = "PAYMENT",
                referenceId = payment.id,
                description = creditPlanName?.let { "${it} 구매" },
            )
        }

        return paymentRepository.save(payment)
    }

    @Transactional
    fun processRefund(
        paymentId: String,
        refundAmount: Long,
        refundReason: String?,
        transactionId: String?,
        responseData: String?,
    ): Payment {
        val payment = getPaymentById(paymentId)

        if (payment.status != PaymentStatus.COMPLETED) {
            throw WorkfolioException("완료된 결제만 환불할 수 있습니다.")
        }

        if (refundAmount > payment.amount) {
            throw WorkfolioException("환불 금액이 결제 금액보다 클 수 없습니다.")
        }

        val refundCredits = if (refundAmount == payment.amount) {
            payment.creditsToAdd
        } else {
            ((refundAmount.toDouble() / payment.amount.toDouble()) * payment.creditsToAdd).toInt()
        }

        if (refundCredits > 0) {
            creditService.refundCredits(
                workerId = payment.worker.id,
                amount = refundCredits,
                referenceType = "PAYMENT_REFUND",
                referenceId = payment.id,
                description = "결제 환불",
            )
        }

        payment.markAsRefunded(refundAmount, refundReason, LocalDateTime.now())

        val transaction = PaymentTransaction(
            paymentId = payment.id,
            transactionType = TransactionType.REFUND,
            status = "DONE",
            amount = -refundAmount,
            transactionId = transactionId,
            responseData = responseData,
        )
        paymentTransactionRepository.save(transaction)

        return paymentRepository.save(payment)
    }

    @Transactional
    fun failPayment(
        paymentId: String,
        failureReason: String?,
        failureCode: String?,
        responseData: String?,
    ): Payment {
        val payment = getPaymentById(paymentId)

        if (payment.status != PaymentStatus.PENDING) {
            throw WorkfolioException("대기 중인 결제만 실패 처리할 수 있습니다.")
        }

        payment.changeStatus(PaymentStatus.FAILED)

        val transaction = PaymentTransaction(
            paymentId = payment.id,
            transactionType = TransactionType.PAYMENT,
            status = "FAILED",
            amount = payment.amount,
            errorMessage = failureReason,
            responseData = responseData,
        )
        paymentTransactionRepository.save(transaction)

        return paymentRepository.save(payment)
    }

    @Transactional(readOnly = true)
    fun getPaymentTransactions(paymentId: String): List<PaymentTransaction> {
        return paymentTransactionRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
    }
}
