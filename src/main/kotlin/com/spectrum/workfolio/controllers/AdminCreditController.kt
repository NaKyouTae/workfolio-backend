package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.credit.CreditHistoryListResponse
import com.spectrum.workfolio.services.CreditService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

data class AdminCreditAdjustRequest(
    val workerId: String,
    val amount: Int,
    val action: String,
    val description: String? = null,
)

@RestController
@RequestMapping("/api/admin/credits")
class AdminCreditController(
    private val creditService: CreditService,
) {

    @GetMapping
    @Transactional(readOnly = true)
    fun getAllCreditHistories(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) txType: String?,
        @RequestParam(required = false) workerId: String?,
    ): CreditHistoryListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        val historyPage = if (workerId != null) {
            if (txType != null) {
                val type = CreditTxType.valueOf(txType)
                creditService.getHistoryByType(workerId, type, pageable)
            } else {
                creditService.getHistory(workerId, pageable)
            }
        } else if (txType != null) {
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
    @Transactional(readOnly = true)
    fun getCreditHistory(@PathVariable id: String): com.spectrum.workfolio.proto.common.CreditHistory {
        val history = creditService.getHistoryById(id)
        return history.toProto(includeWorker = true)
    }

    @DeleteMapping("/{id}")
    fun deleteCreditHistory(@PathVariable id: String): SuccessResponse {
        creditService.deleteHistory(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PostMapping("/adjust")
    fun adjustCreditByAdmin(
        @RequestBody request: AdminCreditAdjustRequest,
    ): SuccessResponse {
        creditService.adjustCreditsByAdmin(
            workerId = request.workerId,
            amount = request.amount,
            action = request.action,
            description = request.description,
        )
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
