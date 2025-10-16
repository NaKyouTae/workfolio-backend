package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyCreateRequest
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyListResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyUpdateRequest
import com.spectrum.workfolio.services.JobSearchCompanyService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/job-search-companies")
class JobSearchCompanyController(
    private val jobSearchCompanyService: JobSearchCompanyService,
) {

    @GetMapping
    fun listJobSearches(
        @RequestParam jobSearchId: String,
    ): JobSearchCompanyListResponse {
        return jobSearchCompanyService.listJobSearchCompanies(jobSearchId)
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

    @DeleteMapping("/{jobSearchCompanyId}")
    fun deleteJobSearch(
        @PathVariable jobSearchCompanyId: String,
    ): SuccessResponse {
        jobSearchCompanyService.deleteJobSearchCompany(jobSearchCompanyId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
