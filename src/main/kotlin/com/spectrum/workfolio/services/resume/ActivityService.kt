package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.Activity
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.ActivityType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.ActivityRepository
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
import com.spectrum.workfolio.utils.EnumUtils.convertProtoEnumSafe
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityService(
    private val resumeQueryService: ResumeQueryService,
    private val activityRepository: ActivityRepository,
) {

    @Transactional(readOnly = true)
    fun getActivity(id: String): Activity {
        return activityRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_ACTIVATE.message) }
    }

    @Transactional(readOnly = true)
    fun listActivities(resumeId: String): List<Activity> {
        return activityRepository.findByResumeIdOrderByPriorityAsc(resumeId)
    }

    @Transactional
    fun createActivity(
        resumeId: String,
        type: ActivityType? = null,
        name: String? = null,
        organization: String? = null,
        certificateNumber: String? = null,
        startedAt: Long? = null,
        endedAt: Long? = null,
        description: String? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): Activity {
        val resume = resumeQueryService.getResume(resumeId)
        val activity = Activity(
            type = type,
            name = name ?: "",
            organization = organization ?: "",
            certificateNumber = certificateNumber ?: "",
            startedAt = TimeUtil.ofEpochMilliNullable(startedAt)?.toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(endedAt)?.toLocalDate(),
            description = description ?: "",
            isVisible = isVisible,
            priority = priority,
            resume = resume,
        )

        return activityRepository.save(activity)
    }

    @Transactional
    fun createBulkActivityFromEntity(
        resume: Resume,
        activities: List<Activity>,
    ) {
        val newActivities = activities.map {
            Activity(
                type = it.type,
                name = it.name,
                organization = it.organization,
                certificateNumber = it.certificateNumber,
                startedAt = it.startedAt,
                endedAt = it.endedAt,
                description = it.description,
                isVisible = it.isVisible,
                priority = it.priority,
                resume = resume,
            )
        }

        activityRepository.saveAll(newActivities)
    }

    @Transactional
    fun updateActivity(
        id: String,
        type: ActivityType? = null,
        name: String? = null,
        organization: String? = null,
        certificateNumber: String? = null,
        startedAt: Long? = null,
        endedAt: Long? = null,
        description: String? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): Activity {
        val activity = this.getActivity(id)

        activity.changeInfo(
            type = type,
            name = name ?: "",
            organization = organization ?: "",
            certificateNumber = certificateNumber ?: "",
            startedAt = TimeUtil.ofEpochMilliNullable(startedAt)?.toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(endedAt)?.toLocalDate(),
            description = description ?: "",
            isVisible = isVisible,
            priority = priority,
        )

        return activityRepository.save(activity)
    }

    @Transactional
    fun createBulkActivity(
        resumeId: String,
        requests: List<ResumeUpdateRequest.ActivityRequest>,
    ): List<Activity> {
        val resume = resumeQueryService.getResume(resumeId)
        val entities = requests.map { request ->
            Activity(
                type = convertProtoEnumSafe<ActivityType>(request.type),
                name = request.name,
                organization = request.organization,
                certificateNumber = request.certificateNumber,
                startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                description = request.description,
                isVisible = request.isVisible,
                priority = request.priority,
                resume = resume,
            )
        }

        return activityRepository.saveAll(entities)
    }

    @Transactional
    fun updateBulkActivity(
        resumeId: String,
        requests: List<ResumeUpdateRequest.ActivityRequest>,
    ): List<Activity> {
        val existingActivities = activityRepository.findByResumeIdOrderByPriorityAsc(resumeId)

        val requestMap = requests.associateBy { it.id }

        val updatedEntities = existingActivities.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    type = convertProtoEnumSafe<ActivityType>(request.type),
                    name = request.name,
                    organization = request.organization,
                    certificateNumber = request.certificateNumber,
                    startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                    endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                    description = request.description,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return activityRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteActivity(id: String) {
        val activity = this.getActivity(id)
        activityRepository.delete(activity)
    }

    @Transactional
    fun deleteActivities(activityIds: List<String>) {
        if (activityIds.isNotEmpty()) {
            activityRepository.deleteAllById(activityIds)
        }
    }

    @Transactional
    fun deleteActivitiesByResumeId(resumeId: String) {
        val activities = activityRepository.findByResumeIdOrderByPriorityAsc(resumeId)
        activityRepository.deleteAll(activities)
    }
}
