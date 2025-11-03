package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.turn_over.TurnOverCreateRequest
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailListResponse
import com.spectrum.workfolio.services.turnovers.TurnOverService
import org.springframework.web.bind.annotation.GetMapping
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
    ): TurnOverDetailListResponse {
        return turnOverService.listTurnOversResult(workerId)
    }

    @PostMapping
    fun upsertTurnOver(
        @AuthenticatedUser workerId: String,
        @RequestBody request: TurnOverCreateRequest,
    ) {
        return turnOverService.upsertTurnOver(workerId, request)
    }
}
