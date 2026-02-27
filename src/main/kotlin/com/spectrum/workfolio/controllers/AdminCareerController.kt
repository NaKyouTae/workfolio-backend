package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.resume.AdminResumeListResponse
import com.spectrum.workfolio.services.resume.ResumeCommandService
import com.spectrum.workfolio.services.resume.ResumeQueryService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/careers")
class AdminCareerController(
    private val resumeQueryService: ResumeQueryService,
    private val resumeCommandService: ResumeCommandService,
) {

    @GetMapping
    fun getCareersByWorkerId(
        @RequestParam workerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminResumeListResponse {
        return resumeQueryService.listAdminResumesByWorkerId(workerId, page, size)
    }

    @GetMapping("/{id}")
    fun getCareerDetail(@PathVariable id: String): com.spectrum.workfolio.proto.common.ResumeDetail {
        return resumeQueryService.getResumeDetailResult(id)
    }

    @DeleteMapping("/{id}")
    fun deleteCareer(@PathVariable id: String): SuccessResponse {
        resumeCommandService.deleteResume(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
