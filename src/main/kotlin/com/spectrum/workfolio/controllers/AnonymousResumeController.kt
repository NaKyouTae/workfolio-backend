package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.proto.resume.PublicResumeDetailResponse
import com.spectrum.workfolio.services.resume.ResumeQueryService
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/anonymous/resumes")
class AnonymousResumeController(
    private val resumeQueryService: ResumeQueryService,
) {

    @GetMapping("/{publicId}")
    fun getPublicResume(
        @PathVariable publicId: String,
    ): PublicResumeDetailResponse {
        val resumeDetail = resumeQueryService.getPublicResumeDetailByPublicId(publicId)
            ?: throw WorkfolioException(MsgKOR.NOT_FOUND_RESUME.message)

        return PublicResumeDetailResponse.newBuilder()
            .setResume(resumeDetail)
            .build()
    }
}
