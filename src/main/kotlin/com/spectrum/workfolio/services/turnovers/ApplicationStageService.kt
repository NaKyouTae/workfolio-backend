package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.ApplicationStage
import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.domain.enums.ApplicationStageStatus
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.ApplicationStageRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicationStageService(
    private val applicationStageRepository: ApplicationStageRepository,
) {
    @Transactional(readOnly = true)
    fun getApplicationStage(id: String): ApplicationStage {
        return applicationStageRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_APPLICATION_STAGE.message)
        }
    }

    @Transactional(readOnly = true)
    fun getApplicationStages(jobApplicationId: String): List<ApplicationStage> {
        return applicationStageRepository.findByJobApplicationIdOrderByPriorityAsc(jobApplicationId)
    }

    // Cascade용: 엔티티만 생성 (저장 X)
    fun createEntity(
        jobApplication: JobApplication,
        request: TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest,
    ): ApplicationStage {
        return ApplicationStage(
            name = request.name,
            status = ApplicationStageStatus.valueOf(request.status.name),
            startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
            memo = request.memo,
            jobApplication = jobApplication,
            isVisible = request.isVisible,
            priority = request.priority,
        )
    }

    @Transactional
    fun create(
        jobApplication: JobApplication,
        request: TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest,
    ): ApplicationStage {
        val applicationStage = createEntity(jobApplication, request)
        return applicationStageRepository.save(applicationStage)
    }

    @Transactional
    fun createBulk(
        jobApplication: JobApplication,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest>,
    ) {
        val entities = requests.map { request ->
            ApplicationStage(
                name = request.name,
                status = ApplicationStageStatus.valueOf(request.status.name),
                startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                memo = request.memo,
                jobApplication = jobApplication,
                isVisible = request.isVisible,
                priority = request.priority,
            )
        }

        applicationStageRepository.saveAll(entities)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest): ApplicationStage {
        val applicationStage = this.getApplicationStage(request.id)

        applicationStage.changeInfo(
            name = request.name,
            status = ApplicationStageStatus.valueOf(request.status.name),
            startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
            memo = request.memo,
            isVisible = request.isVisible,
            priority = request.priority,
        )

        return applicationStageRepository.save(applicationStage)
    }

    @Transactional
    fun updateBulk(
        jobApplicationId: String,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest>,
    ): List<ApplicationStage> {
        val existingApplicationStages = this.getApplicationStages(jobApplicationId)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingApplicationStages.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    name = request.name,
                    status = ApplicationStageStatus.valueOf(request.status.name),
                    startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                    memo = request.memo,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return applicationStageRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteApplicationStages(ids: List<String>) {
        if (ids.isNotEmpty()) {
            applicationStageRepository.deleteAllById(ids)
        }
    }
}
