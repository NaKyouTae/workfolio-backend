package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.interview.InterviewCreateRequest
import com.spectrum.workfolio.proto.interview.InterviewListResponse
import com.spectrum.workfolio.proto.interview.InterviewResponse
import com.spectrum.workfolio.proto.interview.InterviewUpdateRequest
import com.spectrum.workfolio.services.InterviewService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/interviews")
class InterviewController(
    private val interviewService: InterviewService,
) {

    @GetMapping
    fun listInterviews(
        @RequestParam jobSearchCompanyId: String,
    ): InterviewListResponse {
        return interviewService.listInterviews(jobSearchCompanyId)
    }

    @PostMapping
    fun createInterview(
        @RequestBody request: InterviewCreateRequest,
    ): InterviewResponse {
        return interviewService.createInterview(request)
    }

    @PutMapping
    fun updateInterview(
        @RequestBody request: InterviewUpdateRequest,
    ): InterviewResponse {
        return interviewService.updateInterview(request)
    }
}
