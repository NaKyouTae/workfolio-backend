package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.enums.PaymentMethod
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.payment.PaymentGetResponse
import com.spectrum.workfolio.proto.payment.PaymentListResponse
import com.spectrum.workfolio.proto.payment.PaymentRefundRequest
import com.spectrum.workfolio.services.PaymentService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {

    @GetMapping
    fun listPayments(
        @AuthenticatedUser workerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PaymentListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val payments = paymentService.getPaymentsByWorkerId(workerId, pageable)
        val paymentProtos = payments.content.map { it.toProto() }
        return PaymentListResponse.newBuilder()
            .addAllPayments(paymentProtos)
            .build()
    }

    @GetMapping("/{id}")
    fun getPayment(
        @PathVariable id: String,
    ): PaymentGetResponse {
        val payment = paymentService.getPaymentById(id)
        return PaymentGetResponse.newBuilder()
            .setPayment(payment.toProto())
            .build()
    }

    @PostMapping
    fun createPayment(
        @AuthenticatedUser workerId: String,
        @RequestParam creditPlanId: String,
        @RequestParam(defaultValue = "CARD") paymentMethod: String,
    ): PaymentGetResponse {
        val payment = paymentService.createPaymentForCreditPlan(
            workerId = workerId,
            creditPlanId = creditPlanId,
            paymentMethod = PaymentMethod.valueOf(paymentMethod),
        )
        return PaymentGetResponse.newBuilder()
            .setPayment(payment.toProto())
            .build()
    }

    @PostMapping("/confirm")
    fun confirmPayment(
        @RequestParam paymentId: String,
        @RequestParam providerPaymentId: String,
        @RequestParam(required = false) transactionId: String?,
        @RequestParam(required = false) responseData: String?,
        @RequestParam(required = false) receiptUrl: String?,
    ): PaymentGetResponse {
        val payment = paymentService.confirmPayment(
            paymentId = paymentId,
            providerPaymentId = providerPaymentId,
            transactionId = transactionId,
            responseData = responseData,
            receiptUrl = receiptUrl,
        )
        return PaymentGetResponse.newBuilder()
            .setPayment(payment.toProto())
            .build()
    }

    @PostMapping("/{id}/refund")
    fun refundPayment(
        @PathVariable id: String,
        @RequestBody request: PaymentRefundRequest,
    ): SuccessResponse {
        paymentService.processRefund(
            paymentId = id,
            refundAmount = request.refundAmount,
            refundReason = request.refundReason,
            transactionId = null,
            responseData = null,
        )
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
