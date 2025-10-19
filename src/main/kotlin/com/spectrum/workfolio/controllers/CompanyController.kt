package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.company.CompanyCreateRequest
import com.spectrum.workfolio.proto.company.CompanyListResponse
import com.spectrum.workfolio.proto.company.CompanyResponse
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
        @RequestBody request: CompanyCreateRequest,
    ): CompanyResponse {
        return companyService.createCompany(request)
    }

    @PutMapping
    fun updateCompany(
        @RequestBody request: CompanyUpdateRequest,
    ): CompanyResponse {
        return companyService.updateCompany(request)
    }
}
