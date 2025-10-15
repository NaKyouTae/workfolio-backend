package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.interview.Interview
import com.spectrum.workfolio.domain.enums.InterviewType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.InterviewRepository
import com.spectrum.workfolio.proto.interview.InterviewCreateRequest
import com.spectrum.workfolio.proto.interview.InterviewListResponse
import com.spectrum.workfolio.proto.interview.InterviewResponse
import com.spectrum.workfolio.proto.interview.InterviewUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewService(
    private val interviewRepository: InterviewRepository,
    private val jobSearchCompanyService: JobSearchCompanyService,
) {

    @Transactional(readOnly = true)
    fun getInterview(id: String): Interview {
        return interviewRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_JOB_SEARCH.message) }
    }

    @Transactional(readOnly = true)
    fun listInterviews(jobSearchCompanyId: String): InterviewListResponse {
        val interviews = interviewRepository.findByJobSearchCompanyIdOrderByStartedAtDescEndedAtDesc(jobSearchCompanyId)
        return InterviewListResponse.newBuilder()
            .addAllInterviews(interviews.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createInterview(request: InterviewCreateRequest): InterviewResponse {
        val jobSearchCompany = jobSearchCompanyService.getJobSearchCompany(request.jobSearchCompanyId)

        val interview = Interview(
            title = request.title,
            type = InterviewType.valueOf(request.type.name),
            startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt),
            memo = request.memo,
            jobSearchCompany = jobSearchCompany,
        )

        val createdInterview = interviewRepository.save(interview)

        return InterviewResponse.newBuilder().setInterview(createdInterview.toProto()).build()
    }

    @Transactional
    fun updateInterview(request: InterviewUpdateRequest): InterviewResponse {
        val interview = this.getInterview(request.id)

        interview.changeInfo(
            title = request.title,
            type = InterviewType.valueOf(request.type.name),
            startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt),
            memo = request.memo,
        )

        val updatedJobSearchCompany = interviewRepository.save(interview)

        return InterviewResponse.newBuilder().setInterview(updatedJobSearchCompany.toProto()).build()
    }

    @Transactional
    fun deleteInterview(id: String) {
        val interview = this.getInterview(id)
        interviewRepository.delete(interview)
    }
}
