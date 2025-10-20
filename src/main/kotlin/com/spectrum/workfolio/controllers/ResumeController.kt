package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.resume.ResumeCreateRequest
import com.spectrum.workfolio.proto.resume.ResumeListResponse
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
import com.spectrum.workfolio.services.ResumeService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/resumes")
class ResumeController(
    private val resumeService: ResumeService,
) {

    @GetMapping
    fun listResumes(
        @AuthenticatedUser workerId: String,
    ): ResumeListResponse {
        return resumeService.listResumes(workerId)
    }

    @PostMapping
    fun createResume(
        @AuthenticatedUser workerId: String,
        @RequestBody request: ResumeCreateRequest,
    ): SuccessResponse {
        resumeService.createResume(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateResume(
        @AuthenticatedUser workerId: String,
        @RequestBody request: ResumeUpdateRequest,
    ): SuccessResponse {
        resumeService.updateResume(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deleteResume(
        @PathVariable id: String,
        ): SuccessResponse {
        resumeService.deleteResume(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
