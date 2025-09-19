package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.interview.Interview
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.model.MsgKOR
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
    private val workerService: WorkerService,
    private val companyService: CompanyService,
    private val interviewRepository: InterviewRepository,
) {

    @Transactional(readOnly = true)
    fun getInterview(id: String): Interview {
        return interviewRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_INTERVIEW.message) }
    }

    @Transactional(readOnly = true)
    fun listInterviews(workerId: String): InterviewListResponse {
        val interviews = interviewRepository.findByWorkerIdOrderByStartedAtDescEndedAtDesc(workerId)
        return InterviewListResponse.newBuilder()
            .addAllInterviews(interviews.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createInterview(workerId: String, request: InterviewCreateRequest): InterviewResponse {
        val worker = workerService.getWorker(workerId)
        val prevCompany = companyService.getCompany(request.prevCompanyId)
        val nextCompany = if (request.hasNextCompanyId()) {
            companyService.getCompany(request.nextCompanyId)
        } else {
            null
        }

        val interview = Interview(
            title = request.title,
            memo = request.memo,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            prevCompany = prevCompany,
            nextCompany = nextCompany,
            worker = worker,
        )

        val createdPosition = interviewRepository.save(interview)

        return InterviewResponse.newBuilder().setInterview(createdPosition.toProto()).build()
    }

    @Transactional
    fun updateInterview(request: InterviewUpdateRequest): InterviewResponse {
        val interView = this.getInterview(request.id)

        val prevCompany = companyService.getCompany(request.prevCompanyId)
        val nextCompany = if (request.hasNextCompanyId()) {
            companyService.getCompany(request.nextCompanyId)
        } else {
            null
        }

        interView.changeInfo(
            title = request.title,
            memo = request.memo,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            prevCompany = prevCompany,
            nextCompany = nextCompany,
        )

        val updatedInterview = interviewRepository.save(interView)

        return InterviewResponse.newBuilder().setInterview(updatedInterview.toProto()).build()
    }
}
