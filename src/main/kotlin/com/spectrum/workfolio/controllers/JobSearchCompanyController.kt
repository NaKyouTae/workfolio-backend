package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyListResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyUpsertRequest
import com.spectrum.workfolio.services.JobSearchCompanyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/job-searches/{jobSearchId}/companies")
class JobSearchCompanyController(
    private val jobSearchCompanyService: JobSearchCompanyService,
) {

    @GetMapping
    fun listJobSearches(
        @PathVariable jobSearchId: String,
    ): JobSearchCompanyListResponse {
        return jobSearchCompanyService.listJobSearchCompanies(jobSearchId)
    }

    @PostMapping
    fun createJobSearch(
        @PathVariable jobSearchId: String,
        @RequestBody request: JobSearchCompanyUpsertRequest,
    ): JobSearchCompanyResponse {
        return jobSearchCompanyService.createJobSearchCompany(jobSearchId, request)
    }

    @PutMapping("/{jobSearchCompanyId}")
    fun updateJobSearch(
        @PathVariable jobSearchId: String,
        @PathVariable jobSearchCompanyId: String,
        @RequestBody request: JobSearchCompanyUpsertRequest,
    ): JobSearchCompanyResponse {
        return jobSearchCompanyService.updateJobSearchCompany(jobSearchCompanyId, request)
    }
}
