package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.salary.SalaryCreateRequest
import com.spectrum.workfolio.proto.salary.SalaryListResponse
import com.spectrum.workfolio.proto.salary.SalaryResponse
import com.spectrum.workfolio.proto.salary.SalaryUpdateRequest
import com.spectrum.workfolio.services.SalaryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/salaries")
class SalaryController(
    private val salaryService: SalaryService,
) {

    @GetMapping
    fun listCompanies(
        @RequestParam companiesIds: List<String>,
    ): SalaryListResponse {
        return salaryService.listSalaries(companiesIds)
    }

    @PostMapping
    fun createCompany(
        @RequestBody request: SalaryCreateRequest,
    ): SalaryResponse {
        return salaryService.createSalary(request)
    }

    @PutMapping
    fun updateCompany(
        @RequestBody request: SalaryUpdateRequest,
    ): SalaryResponse {
        return salaryService.updateSalary(request)
    }
}
