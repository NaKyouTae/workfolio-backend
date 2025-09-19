package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.education.EducationCreateRequest
import com.spectrum.workfolio.proto.education.EducationListResponse
import com.spectrum.workfolio.proto.education.EducationUpdateRequest
import com.spectrum.workfolio.services.EducationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/educations")
class EducationController(
    private val educationService: EducationService,
) {

    @GetMapping
    fun getEducations(
        @AuthenticatedUser workerId: String,
    ): EducationListResponse {
        return educationService.listEducations(workerId)
    }

    @PostMapping
    fun createEducation(
        @AuthenticatedUser workerId: String,
        @RequestBody request: EducationCreateRequest,
    ): SuccessResponse {
        educationService.createEducation(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateEducation(
        @RequestBody request: EducationUpdateRequest,
    ): SuccessResponse {
        educationService.updateEducation(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
