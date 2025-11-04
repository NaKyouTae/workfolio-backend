package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.services.turnovers.TurnOverService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/turn-overs")
class TurnOverController(
    private val turnOverService: TurnOverService,
) {

    @GetMapping
    fun listTurnOvers(
        @AuthenticatedUser workerId: String,
    ): TurnOverListResponse {
        return turnOverService.listTurnOversResult(workerId)
    }

    @GetMapping("/details/{id}")
    fun getTurnOverDetail(
        @PathVariable id: String,
    ): TurnOverDetailResponse {
        return turnOverService.getTurnOverDetailResult(id)
    }

    @PostMapping
    fun upsertTurnOver(
        @AuthenticatedUser workerId: String,
        @RequestBody request: TurnOverUpsertRequest,
    ): SuccessResponse {
        turnOverService.upsertTurnOver(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PostMapping("/{id}/duplicate")
    fun duplicateTurnOver(
        @PathVariable id: String,
    ): SuccessResponse {
        turnOverService.duplicate(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deleteTurnOver(
        @PathVariable id: String,
    ): SuccessResponse {
        turnOverService.delete(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
