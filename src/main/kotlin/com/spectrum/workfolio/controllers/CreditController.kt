package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.proto.credit.CreditBalanceResponse
import com.spectrum.workfolio.proto.credit.CreditHistoryListResponse
import com.spectrum.workfolio.proto.credit.CreditUseRequest
import com.spectrum.workfolio.proto.credit.CreditUseResponse
import com.spectrum.workfolio.services.CreditService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/credits")
class CreditController(
    private val creditService: CreditService,
) {

    @GetMapping
    fun getBalance(
        @AuthenticatedUser workerId: String,
    ): CreditBalanceResponse {
        val balance = creditService.getBalance(workerId)
        return CreditBalanceResponse.newBuilder()
            .setBalance(balance)
            .build()
    }

    @GetMapping("/history")
    fun getHistory(
        @AuthenticatedUser workerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) txType: String?,
    ): CreditHistoryListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        val historyPage = if (txType != null) {
            val type = CreditTxType.valueOf(txType)
            creditService.getHistoryByType(workerId, type, pageable)
        } else {
            creditService.getHistory(workerId, pageable)
        }

        val historyProtos = historyPage.content.map { it.toProto() }

        return CreditHistoryListResponse.newBuilder()
            .addAllCreditHistories(historyProtos)
            .setTotalElements(historyPage.totalElements.toInt())
            .setTotalPages(historyPage.totalPages)
            .setCurrentPage(page)
            .build()
    }

    @PostMapping("/use")
    fun useCredits(
        @AuthenticatedUser workerId: String,
        @RequestBody request: CreditUseRequest,
    ): CreditUseResponse {
        val history = creditService.useCredits(
            workerId = workerId,
            amount = request.amount,
            referenceType = if (request.hasReferenceType()) request.referenceType else null,
            referenceId = if (request.hasReferenceId()) request.referenceId else null,
            description = if (request.hasDescription()) request.description else null,
        )

        return CreditUseResponse.newBuilder()
            .setBalanceBefore(history.balanceBefore)
            .setBalanceAfter(history.balanceAfter)
            .setAmountUsed(history.amount)
            .setCreditHistory(history.toProto())
            .build()
    }
}
