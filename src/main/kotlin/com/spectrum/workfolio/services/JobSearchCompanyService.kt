package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.interview.JobSearchCompany
import com.spectrum.workfolio.domain.enums.JobSearchCompanyStatus
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.JobSearchCompanyRepository
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyListResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyResponse
import com.spectrum.workfolio.proto.job_search_company.JobSearchCompanyUpsertRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JobSearchCompanyService(
    private val jobSearchService: JobSearchService,
    private val jobSearchCompanyRepository: JobSearchCompanyRepository,
) {

    @Transactional(readOnly = true)
    fun getJobSearchCompany(id: String): JobSearchCompany {
        return jobSearchCompanyRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_JOB_SEARCH.message) }
    }

    @Transactional(readOnly = true)
    fun listJobSearchCompanies(jobSearchId: String): JobSearchCompanyListResponse {
        val jobSearchCompanies = jobSearchCompanyRepository.findByJobSearchIdOrderByAppliedAtDescClosedAtDesc(jobSearchId)
        return JobSearchCompanyListResponse.newBuilder()
            .addAllJobSearchCompanies(jobSearchCompanies.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createJobSearchCompany(jobSearchId: String, request: JobSearchCompanyUpsertRequest): JobSearchCompanyResponse {
        val jobSearch = jobSearchService.getJobSearch(jobSearchId)

        val jobSearchCompany = JobSearchCompany(
            name = request.name,
            status = JobSearchCompanyStatus.valueOf(request.status.name),
            appliedAt = TimeUtil.ofEpochMilliNullable(request.appliedAt),
            closedAt = TimeUtil.ofEpochMilliNullable(request.closedAt),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            industry = request.industry,
            location = request.location,
            businessSize = request.businessSize,
            description = request.description,
            memo = request.memo,
            link = request.link,
            jobSearch = jobSearch,
        )

        val createdJobSearchCompany = jobSearchCompanyRepository.save(jobSearchCompany)

        return JobSearchCompanyResponse.newBuilder().setJobSearchCompany(createdJobSearchCompany.toProto()).build()
    }

    @Transactional
    fun updateJobSearchCompany(jobSearchCompanyId: String, request: JobSearchCompanyUpsertRequest): JobSearchCompanyResponse {

        val jobSearchCompany = this.getJobSearchCompany(jobSearchCompanyId)

        jobSearchCompany.changeInfo(
            name = request.name,
            status = JobSearchCompanyStatus.valueOf(request.status.name),
            appliedAt = TimeUtil.ofEpochMilli(request.appliedAt),
            closedAt = TimeUtil.ofEpochMilli(request.closedAt),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            industry = request.industry,
            location = request.location,
            businessSize = request.businessSize,
            description = request.description,
            memo = request.memo,
            link = request.link,
        )

        val updatedJobSearchCompany = jobSearchCompanyRepository.save(jobSearchCompany)

        return JobSearchCompanyResponse.newBuilder().setJobSearchCompany(updatedJobSearchCompany.toProto()).build()
    }
}
