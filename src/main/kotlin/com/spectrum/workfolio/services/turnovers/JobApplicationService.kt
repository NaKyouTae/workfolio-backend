package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.domain.enums.JobApplicationStatus
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.JobApplicationRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JobApplicationService(
    private val jobApplicationRepository: JobApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun getJobApplication(id: String): JobApplication {
        return jobApplicationRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_JOB_APPLICATION.message)
        }
    }

    @Transactional(readOnly = true)
    fun getJobApplications(turnOverChallengeId: String): List<JobApplication> {
        return jobApplicationRepository.findByTurnOverChallengeIdOrderByPriorityAsc(turnOverChallengeId)
    }

    // Cascade용: 엔티티만 생성 (저장 X)
    fun createEntity(
        turnOverChallenge: TurnOverChallenge,
        request: TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest,
    ): JobApplication {
        return JobApplication(
            name = request.name,
            status = JobApplicationStatus.valueOf(request.status.name),
            position = request.position,
            jobPostingTitle = request.jobPostingTitle,
            jobPostingUrl = request.jobPostingUrl,
            startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            applicationSource = request.applicationSource,
            memo = request.memo,
            turnOverChallenge = turnOverChallenge,
            isVisible = request.isVisible,
            priority = request.priority,
        )
    }

    @Transactional
    fun create(
        turnOverChallenge: TurnOverChallenge,
        request: TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest,
    ): JobApplication {
        val jobApplication = createEntity(turnOverChallenge, request)
        return jobApplicationRepository.save(jobApplication)
    }

    @Transactional
    fun createBulk(
        turnOverChallenge: TurnOverChallenge,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest>,
    ): List<JobApplication> {
        val entities = requests.map { request ->
            JobApplication(
                name = request.name,
                status = JobApplicationStatus.valueOf(request.status.name),
                position = request.position,
                jobPostingTitle = request.jobPostingTitle,
                jobPostingUrl = request.jobPostingUrl,
                startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                applicationSource = request.applicationSource,
                memo = request.memo,
                turnOverChallenge = turnOverChallenge,
                isVisible = request.isVisible,
                priority = request.priority,
            )
        }

        return jobApplicationRepository.saveAll(entities)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest): JobApplication {
        val jobApplication = this.getJobApplication(request.id)

        jobApplication.changeInfo(
            name = request.name,
            status = JobApplicationStatus.valueOf(request.status.name),
            position = request.position,
            jobPostingTitle = request.jobPostingTitle,
            jobPostingUrl = request.jobPostingUrl,
            startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            applicationSource = request.applicationSource,
            memo = request.memo,
            isVisible = request.isVisible,
            priority = request.priority,
        )

        return jobApplicationRepository.save(jobApplication)
    }

    @Transactional
    fun updateBulk(
        turnOverChallengeId: String,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest>,
    ): List<JobApplication> {
        val existingJobApplications = this.getJobApplications(turnOverChallengeId)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingJobApplications.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    name = request.name,
                    status = JobApplicationStatus.valueOf(request.status.name),
                    position = request.position,
                    jobPostingTitle = request.jobPostingTitle,
                    jobPostingUrl = request.jobPostingUrl,
                    startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                    endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                    applicationSource = request.applicationSource,
                    memo = request.memo,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return jobApplicationRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteJobApplications(ids: List<String>) {
        if (ids.isNotEmpty()) {
            jobApplicationRepository.deleteAllById(ids)
        }
    }
}
