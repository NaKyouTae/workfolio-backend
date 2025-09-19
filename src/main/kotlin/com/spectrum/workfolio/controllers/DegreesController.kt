package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.degrees.DegreesCreateRequest
import com.spectrum.workfolio.proto.degrees.DegreesListResponse
import com.spectrum.workfolio.proto.degrees.DegreesUpdateRequest
import com.spectrum.workfolio.proto.education.EducationCreateRequest
import com.spectrum.workfolio.proto.education.EducationListResponse
import com.spectrum.workfolio.proto.education.EducationUpdateRequest
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
        @AuthenticatedUser workerId: String,
        @RequestBody request: DegreesCreateRequest,
    ): SuccessResponse {
        degreesService.createDegrees(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateDegrees(
        @RequestBody request: DegreesUpdateRequest,
    ): SuccessResponse {
        degreesService.updateDegrees(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
