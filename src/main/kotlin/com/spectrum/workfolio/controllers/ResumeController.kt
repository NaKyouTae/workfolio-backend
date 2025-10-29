package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.resume.ResumeDetailListResponse
import com.spectrum.workfolio.proto.resume.ResumeListResponse
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
import com.spectrum.workfolio.services.ResumeCommandService
import com.spectrum.workfolio.services.ResumeQueryService
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
    private val resumeQueryService: ResumeQueryService,
    private val resumeCommandService: ResumeCommandService,
) {

    @GetMapping
    fun listResumes(
        @AuthenticatedUser workerId: String,
    ): ResumeListResponse {
        return resumeQueryService.listResumesResult(workerId)
    }

    @GetMapping("/details")
    fun listResumeDetails(
        @AuthenticatedUser workerId: String,
    ): ResumeDetailListResponse {
        return resumeQueryService.listResumeDetailsResult(workerId)
    }

    @PostMapping("/{id}/duplicate")
    fun duplicateResume(
        @PathVariable id: String,
    ): SuccessResponse {
        resumeCommandService.duplicateResume(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateResume(
        @AuthenticatedUser workerId: String,
        @RequestBody request: ResumeUpdateRequest,
    ): SuccessResponse {
        resumeCommandService.updateResume(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deleteResume(
        @PathVariable id: String,
    ): SuccessResponse {
        resumeCommandService.deleteResume(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
