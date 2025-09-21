package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.job_search.JobSearchListResponse
import com.spectrum.workfolio.proto.job_search.JobSearchResponse
import com.spectrum.workfolio.proto.job_search.JobSearchUpsertRequest
import com.spectrum.workfolio.services.JobSearchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/job-searches")
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
        @RequestBody request: JobSearchUpsertRequest,
    ): JobSearchResponse {
        return jobSearchService.createJobSearch(workerId, request)
    }

    @PutMapping("/{jobSearchId}")
    fun updateJobSearch(
        @PathVariable jobSearchId: String,
        @RequestBody request: JobSearchUpsertRequest,
    ): JobSearchResponse {
        return jobSearchService.updateJobSearch(jobSearchId, request)
    }
}
