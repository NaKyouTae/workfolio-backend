package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.company.CompanyCreateRequest
import com.spectrum.workfolio.proto.company.CompanyListResponse
import com.spectrum.workfolio.proto.company.CompanyUpdateRequest
import com.spectrum.workfolio.services.CompanyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/companies")
class CompanyController(
    private val companyService: CompanyService,
) {

    @GetMapping
    fun listCompanies(
        @AuthenticatedUser workerId: String,
    ): CompanyListResponse {
        return companyService.listCompanies(workerId)
    }

    @PostMapping
    fun createCompany(
        @AuthenticatedUser workerId: String,
        @RequestBody request: CompanyCreateRequest,
    ): SuccessResponse {
        companyService.createCompany(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateCompany(
        @AuthenticatedUser workerId: String,
        @RequestBody request: CompanyUpdateRequest,
    ): SuccessResponse {
        companyService.updateCompany(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
