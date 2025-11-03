package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.Activity
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.ActivityType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.ActivityRepository
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
            startedAt = if (startedAt != null && startedAt > 0) TimeUtil.ofEpochMilli(startedAt).toLocalDate() else null,
            endedAt = if (endedAt != null && endedAt > 0) TimeUtil.ofEpochMilli(endedAt).toLocalDate() else null,
            description = description ?: "",
            isVisible = isVisible,
            priority = priority,
            resume = resume,
        )

        return activityRepository.save(activity)
    }

    @Transactional
    fun createBulkActivity(
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
            startedAt = if (startedAt != null && startedAt > 0) TimeUtil.ofEpochMilli(startedAt).toLocalDate() else null,
            endedAt = if (endedAt != null && endedAt > 0) TimeUtil.ofEpochMilli(endedAt).toLocalDate() else null,
            description = description ?: "",
            isVisible = isVisible,
            priority = priority,
        )

        return activityRepository.save(activity)
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
