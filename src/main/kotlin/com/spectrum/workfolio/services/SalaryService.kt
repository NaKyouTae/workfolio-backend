package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Salary
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.SalaryRepository
import com.spectrum.workfolio.proto.salary.SalaryCreateRequest
import com.spectrum.workfolio.proto.salary.SalaryListResponse
import com.spectrum.workfolio.proto.salary.SalaryResponse
import com.spectrum.workfolio.proto.salary.SalaryUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SalaryService(
    private val companyService: CompanyService,
    private val salaryRepository: SalaryRepository,
) {

    @Transactional(readOnly = true)
    fun getSalary(id: String): Salary {
        return salaryRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_SALARY.message) }
    }

    @Transactional(readOnly = true)
    fun listSalaries(companyIds: List<String>): SalaryListResponse {
        val salaries = salaryRepository.findByCompanyIdInOrderByStartedAtDescEndedAtDesc(companyIds)
        return SalaryListResponse.newBuilder()
            .addAllSalaries(salaries.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createSalary(request: SalaryCreateRequest): SalaryResponse {
        val company = companyService.getCompany(request.companyId)
        val position = Salary(
            amount = request.amount,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            company = company,
        )

        val createdSalary = salaryRepository.save(position)

        return SalaryResponse.newBuilder().setSalary(createdSalary.toProto()).build()
    }

    @Transactional
    fun updateSalary(request: SalaryUpdateRequest): SalaryResponse {
        val salary = this.getSalary(request.id)

        salary.changeInfo(
            amount = request.amount,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
        )

        val updatedSalary = salaryRepository.save(salary)

        return SalaryResponse.newBuilder().setSalary(updatedSalary.toProto()).build()
    }
}
