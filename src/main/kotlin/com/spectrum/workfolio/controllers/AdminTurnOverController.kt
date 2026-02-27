package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailResponse
import com.spectrum.workfolio.proto.turn_over.AdminTurnOverListResponse
import com.spectrum.workfolio.services.turnovers.TurnOverService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/turn-overs")
class AdminTurnOverController(
    private val turnOverService: TurnOverService,
) {

    @GetMapping
    fun getTurnOversByWorkerId(
        @RequestParam workerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminTurnOverListResponse {
        return turnOverService.listAdminTurnOversResult(workerId, page, size)
    }

    @GetMapping("/{id}")
    fun getTurnOverDetail(@PathVariable id: String): TurnOverDetailResponse {
        return turnOverService.getTurnOverDetailResult(id)
    }

    @DeleteMapping("/{id}")
    fun deleteTurnOver(@PathVariable id: String): SuccessResponse {
        turnOverService.delete(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
