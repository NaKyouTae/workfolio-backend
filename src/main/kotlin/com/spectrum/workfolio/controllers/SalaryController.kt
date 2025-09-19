package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.salary.SalaryCreateRequest
import com.spectrum.workfolio.proto.salary.SalaryListResponse
import com.spectrum.workfolio.proto.salary.SalaryUpdateRequest
import com.spectrum.workfolio.services.SalaryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/salaries")
class SalaryController(
    private val salaryService: SalaryService,
) {

    @GetMapping("/{companyId}")
    fun listCompanies(
        @PathVariable companyId: String,
    ): SalaryListResponse {
        return salaryService.listSalaries(companyId)
    }

    @PostMapping
    fun createCompany(
        @RequestBody request: SalaryCreateRequest,
    ): SuccessResponse {
        salaryService.createSalary(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateCompany(
        @RequestBody request: SalaryUpdateRequest,
    ): SuccessResponse {
        salaryService.updateSalary(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
