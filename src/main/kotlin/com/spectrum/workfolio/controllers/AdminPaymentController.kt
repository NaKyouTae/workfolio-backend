package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.payment.PaymentListResponse
import com.spectrum.workfolio.services.PaymentService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/payments")
class AdminPaymentController(
    private val paymentService: PaymentService,
) {

    @GetMapping
    fun getPaymentsByWorkerId(
        @RequestParam workerId: String,
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

    @DeleteMapping("/{id}")
    fun deletePayment(@PathVariable id: String): SuccessResponse {
        paymentService.deletePayment(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
