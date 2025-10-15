package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.job_search.JobSearchCreateRequest
import com.spectrum.workfolio.proto.job_search.JobSearchListResponse
import com.spectrum.workfolio.proto.job_search.JobSearchResponse
import com.spectrum.workfolio.proto.job_search.JobSearchUpdateRequest
import com.spectrum.workfolio.services.JobSearchService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/job-searches")
class JobSearchController(
    private val jobSearchService: JobSearchService,
) {

    @GetMapping
    fun listJobSearches(
        @AuthenticatedUser workerId: String,
    ): JobSearchListResponse {
        return jobSearchService.listJobSearches(workerId)
    }

    @PostMapping
    fun createJobSearch(
        @AuthenticatedUser workerId: String,
        @RequestBody request: JobSearchCreateRequest,
    ): JobSearchResponse {
        return jobSearchService.createJobSearch(workerId, request)
    }

    @PutMapping
    fun updateJobSearch(
        @RequestBody request: JobSearchUpdateRequest,
    ): JobSearchResponse {
        return jobSearchService.updateJobSearch(request)
    }

    @DeleteMapping("/{jobSearchId}")
    fun deleteJobSearch(
        @PathVariable jobSearchId: String,
    ): SuccessResponse {
        jobSearchService.deleteJobSearch(jobSearchId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
