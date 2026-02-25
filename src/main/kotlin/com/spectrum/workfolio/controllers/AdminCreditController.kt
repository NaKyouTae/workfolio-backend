package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.credit.CreditHistoryListResponse
import com.spectrum.workfolio.services.CreditService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/credits")
class AdminCreditController(
    private val creditService: CreditService,
) {

    @GetMapping
    fun getAllCreditHistories(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) txType: String?,
    ): CreditHistoryListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        val historyPage = if (txType != null) {
            val type = CreditTxType.valueOf(txType)
            creditService.getAllHistoryByType(type, pageable)
        } else {
            creditService.getAllHistory(pageable)
        }

        val historyProtos = historyPage.content.map { it.toProto(includeWorker = true) }

        return CreditHistoryListResponse.newBuilder()
            .addAllCreditHistories(historyProtos)
            .setTotalElements(historyPage.totalElements.toInt())
            .setTotalPages(historyPage.totalPages)
            .setCurrentPage(page)
            .build()
    }

    @GetMapping("/{id}")
    fun getCreditHistory(@PathVariable id: String): com.spectrum.workfolio.proto.common.CreditHistory {
        val history = creditService.getHistoryById(id)
        return history.toProto(includeWorker = true)
    }
}
