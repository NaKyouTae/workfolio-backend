package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.position.PositionCreateRequest
import com.spectrum.workfolio.proto.position.PositionListResponse
import com.spectrum.workfolio.proto.position.PositionResponse
import com.spectrum.workfolio.proto.position.PositionUpdateRequest
import com.spectrum.workfolio.services.PositionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/positions")
class PositionController(
    private val positionService: PositionService,
) {

    @GetMapping
    fun listPositions(
        @RequestParam companiesIds: List<String>,
    ): PositionListResponse {
        return positionService.listPositions(companiesIds)
    }

    @PostMapping
    fun createPosition(
        @RequestBody request: PositionCreateRequest,
    ): PositionResponse {
        return positionService.createPosition(request)
    }

    @PutMapping
    fun updatePosition(
        @RequestBody request: PositionUpdateRequest,
    ): PositionResponse {
        return positionService.updatePosition(request)
    }
}
