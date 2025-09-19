package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.position.PositionCreateRequest
import com.spectrum.workfolio.proto.position.PositionListResponse
import com.spectrum.workfolio.proto.position.PositionUpdateRequest
import com.spectrum.workfolio.services.PositionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/positions")
class PositionController(
    private val positionService: PositionService,
) {

    @GetMapping("/{companyId}")
    fun listCompanies(
        @PathVariable companyId: String,
    ): PositionListResponse {
        return positionService.listPositions(companyId)
    }

    @PostMapping
    fun createCompany(
        @RequestBody request: PositionCreateRequest,
    ): SuccessResponse {
        positionService.createPosition(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateCompany(
        @RequestBody request: PositionUpdateRequest,
    ): SuccessResponse {
        positionService.updatePosition(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
