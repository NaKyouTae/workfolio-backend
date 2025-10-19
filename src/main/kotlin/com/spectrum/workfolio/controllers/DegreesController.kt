package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.degrees.DegreesCreateRequest
import com.spectrum.workfolio.proto.degrees.DegreesListResponse
import com.spectrum.workfolio.proto.degrees.DegreesResponse
import com.spectrum.workfolio.proto.degrees.DegreesUpdateRequest
import com.spectrum.workfolio.services.DegreesService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/degrees")
class DegreesController(
    private val degreesService: DegreesService,
) {

    @GetMapping
    fun getDegrees(
        @AuthenticatedUser workerId: String,
    ): DegreesListResponse {
        return degreesService.listDegrees(workerId)
    }

    @PostMapping
    fun createDegrees(
        @RequestBody request: DegreesCreateRequest,
    ): DegreesResponse {
        return degreesService.createDegrees(request)
    }

    @PutMapping
    fun updateDegrees(
        @RequestBody request: DegreesUpdateRequest,
    ): DegreesResponse {
        return degreesService.updateDegrees(request)
    }
}
