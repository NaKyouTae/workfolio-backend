package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.interview.JobSearch
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.JobSearchRepository
import com.spectrum.workfolio.proto.job_search.JobSearchCreateRequest
import com.spectrum.workfolio.proto.job_search.JobSearchListResponse
import com.spectrum.workfolio.proto.job_search.JobSearchResponse
import com.spectrum.workfolio.proto.job_search.JobSearchUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JobSearchService(
    private val workerService: WorkerService,
    private val companyService: CompanyService,
    private val jobSearchRepository: JobSearchRepository,
) {

    @Transactional(readOnly = true)
    fun getJobSearch(id: String): JobSearch {
        return jobSearchRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_JOB_SEARCH.message) }
    }

    @Transactional(readOnly = true)
    fun listJobSearches(workerId: String): JobSearchListResponse {
        val jobSearches = jobSearchRepository.findByWorkerIdOrderByStartedAtDescEndedAtDesc(workerId)
        return JobSearchListResponse.newBuilder()
            .addAllJobSearches(jobSearches.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createJobSearch(workerId: String, request: JobSearchCreateRequest): JobSearchResponse {
        val worker = workerService.getWorker(workerId)
        val prevCompany = companyService.getCompany(request.prevCompanyId)
        val nextCompany = if (request.hasNextCompanyId()) {
            companyService.getCompany(request.nextCompanyId)
        } else {
            null
        }

        val jobSearch = JobSearch(
            title = request.title,
            memo = request.memo,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            prevCompany = prevCompany,
            nextCompany = nextCompany,
            worker = worker,
        )

        val createdJobSearch = jobSearchRepository.save(jobSearch)

        return JobSearchResponse.newBuilder().setJobSearch(createdJobSearch.toProto()).build()
    }

    @Transactional
    fun updateJobSearch(request: JobSearchUpdateRequest): JobSearchResponse {
        val jobSearch = this.getJobSearch(request.id)

        val prevCompany = companyService.getCompany(request.prevCompanyId)
        val nextCompany = if (request.hasNextCompanyId()) {
            companyService.getCompany(request.nextCompanyId)
        } else {
            null
        }

        jobSearch.changeInfo(
            title = request.title,
            memo = request.memo,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            prevCompany = prevCompany,
            nextCompany = nextCompany,
        )

        val updatedJobSearch = jobSearchRepository.save(jobSearch)

        return JobSearchResponse.newBuilder().setJobSearch(updatedJobSearch.toProto()).build()
    }
}
