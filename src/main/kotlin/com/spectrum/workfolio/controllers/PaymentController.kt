package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.payment.PaymentGetResponse
import com.spectrum.workfolio.proto.payment.PaymentListResponse
import com.spectrum.workfolio.services.PaymentService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    fun listPaymentsByOrder(
        @RequestParam orderId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PaymentListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val payments = paymentService.getPaymentsByOrderId(orderId, pageable)
        val paymentProtos = payments.content.map { it.toProto() }
        return PaymentListResponse.newBuilder()
            .addAllPayments(paymentProtos)
            .build()
    }
}
