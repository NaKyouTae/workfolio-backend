package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.history.Company
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.CompanyRepository
import com.spectrum.workfolio.proto.company.CompanyCreateRequest
import com.spectrum.workfolio.proto.company.CompanyListResponse
import com.spectrum.workfolio.proto.company.CompanyResponse
import com.spectrum.workfolio.proto.company.CompanyUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompanyService(
    private val workerService: WorkerService,
    private val companyRepository: CompanyRepository,
) {

    @Transactional(readOnly = true)
    fun getCompany(id: String): Company {
        return companyRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_COMPANY.message) }
    }

    @Transactional(readOnly = true)
    fun listCompanies(workerId: String): CompanyListResponse {
        val companies = companyRepository.findByWorkerIdOrderByStartedAtDescEndedAtDesc(workerId)
        return CompanyListResponse.newBuilder()
            .addAllCompanies(companies.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createCompany(workerId: String, request: CompanyCreateRequest): CompanyResponse {
        val worker = workerService.getWorker(workerId)
        val company = Company(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            isWorking = request.isWorking,
            worker = worker,
        )

        val createdCompany = companyRepository.save(company)

        return CompanyResponse.newBuilder().setCompany(createdCompany.toProto()).build()
    }

    @Transactional
    fun updateCompany(request: CompanyUpdateRequest): CompanyResponse {
        val company = this.getCompany(request.id)

        company.changeInfo(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            isWorking = request.isWorking,
        )

        val createdCompany = companyRepository.save(company)

        return CompanyResponse.newBuilder().setCompany(createdCompany.toProto()).build()
    }
}
