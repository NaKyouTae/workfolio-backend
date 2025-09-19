package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyCreateRequest
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyListResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyUpdateRequest
import com.spectrum.workfolio.services.JobSearchCompanyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/job-search-companies")
class JobSearchCompanyController(
    private val jobSearchCompanyService: JobSearchCompanyService,
) {

    @GetMapping
    fun listJobSearches(
        @AuthenticatedUser workerId: String,
    ): JobSearchCompanyListResponse {
        return jobSearchCompanyService.listJobCompanies(workerId)
    }

    @PostMapping
    fun createJobSearch(
        @RequestBody request: JobSearchCompanyCreateRequest,
    ): JobSearchCompanyResponse {
        return jobSearchCompanyService.createJobSearchCompany(request)
    }

    @PutMapping
    fun updateJobSearch(
        @RequestBody request: JobSearchCompanyUpdateRequest,
    ): JobSearchCompanyResponse {
        return jobSearchCompanyService.updateJobSearchCompany(request)
    }
}
