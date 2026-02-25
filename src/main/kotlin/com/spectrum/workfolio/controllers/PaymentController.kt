package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.payment.PaymentConfirmRequest
import com.spectrum.workfolio.proto.payment.PaymentConfirmResponse
import com.spectrum.workfolio.proto.payment.PaymentCreateRequest
import com.spectrum.workfolio.proto.payment.PaymentCreateResponse
import com.spectrum.workfolio.proto.payment.PaymentGetResponse
import com.spectrum.workfolio.proto.payment.PaymentListResponse
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

    @GetMapping("/{id}")
    fun getPayment(
        @PathVariable id: String,
    ): PaymentGetResponse {
        val payment = paymentService.getPaymentById(id)
        return PaymentGetResponse.newBuilder()
            .setPayment(payment.toProto())
            .build()
    }

    @GetMapping
    fun listPayments(
        @AuthenticatedUser workerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PaymentListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val paymentsPage = paymentService.getPaymentsByWorkerId(workerId, pageable)
        val paymentProtos = paymentsPage.content.map { it.toProto() }
        return PaymentListResponse.newBuilder()
            .addAllPayments(paymentProtos)
            .setTotalElements(paymentsPage.totalElements.toInt())
            .setTotalPages(paymentsPage.totalPages)
            .setCurrentPage(page)
            .build()
    }

    @PostMapping
    fun createPayment(
        @AuthenticatedUser workerId: String,
        @RequestBody request: PaymentCreateRequest,
    ): PaymentCreateResponse {
        val paymentMethod = request.paymentMethod.name
        val payment = paymentService.createPayment(workerId, request.creditPlanId, paymentMethod)

        return PaymentCreateResponse.newBuilder()
            .setPayment(payment.toProto())
            .setOrderId(payment.id)
            .setAmount(payment.amount.toLong())
            .build()
    }

    @PostMapping("/confirm")
    fun confirmPayment(
        @AuthenticatedUser workerId: String,
        @RequestBody request: PaymentConfirmRequest,
    ): PaymentConfirmResponse {
        val payment = paymentService.confirmPayment(request.paymentId, request.providerPaymentId)

        return PaymentConfirmResponse.newBuilder()
            .setPayment(payment.toProto())
            .build()
    }
}
