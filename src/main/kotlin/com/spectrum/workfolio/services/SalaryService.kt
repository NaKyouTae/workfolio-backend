package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Career
import com.spectrum.workfolio.domain.entity.resume.Salary
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.SalaryRepository
import com.spectrum.workfolio.proto.salary.SalaryListResponse
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SalaryService(
    private val careerService: CareerService,
    private val salaryRepository: SalaryRepository,
) {

    @Transactional(readOnly = true)
    fun getSalary(id: String): Salary {
        return salaryRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_SALARY.message) }
    }

    @Transactional(readOnly = true)
    fun listSalaries(careerId: String): SalaryListResponse {
        val salaries = salaryRepository.findByCareerIdOrderByPriorityAscNegotiationDateDesc(careerId)
        return SalaryListResponse.newBuilder()
            .addAllSalaries(salaries.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createBulkSalary(
        career: Career,
        salaries: List<Salary>,
    ) {
        val newSalaries = salaries.map {
            Salary(
                amount = it.amount,
                memo = it.memo,
                negotiationDate = it.negotiationDate,
                isVisible = it.isVisible,
                priority = it.priority,
                career = career,
            )
        }

        salaryRepository.saveAll(newSalaries)
    }

    @Transactional
    fun deleteSalaries(salaries: List<Salary>) {
        if (salaries.isNotEmpty()) {
            salaryRepository.deleteAll(salaries)
        }
    }
}
