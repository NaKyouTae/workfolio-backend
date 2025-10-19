package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.education.EducationCreateRequest
import com.spectrum.workfolio.proto.education.EducationListResponse
import com.spectrum.workfolio.proto.education.EducationResponse
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
        @RequestBody request: EducationCreateRequest,
    ): EducationResponse {
        return educationService.createEducation(request)
    }

    @PutMapping
    fun updateEducation(
        @RequestBody request: EducationUpdateRequest,
    ): EducationResponse {
        return educationService.updateEducation(request)
    }
}
