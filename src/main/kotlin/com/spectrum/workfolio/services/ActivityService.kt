package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Activity
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
        return activityRepository.findByResumeId(resumeId)
    }

    @Transactional
    fun createActivity(
        resumeId: String,
        type: ActivityType?,
        name: String?,
        organization: String?,
        certificateNumber: String?,
        startedAt: Long?,
        endedAt: Long?,
        description: String?,
        isVisible: Boolean?,
    ): Activity {
        val resume = resumeQueryService.getResume(resumeId)
        val activity = Activity(
            type = type,
            name = name,
            organization = organization,
            certificateNumber = certificateNumber,
            startedAt = if (startedAt != null && startedAt > 0) TimeUtil.ofEpochMilli(startedAt).toLocalDate() else null,
            endedAt = if (endedAt != null && endedAt > 0) TimeUtil.ofEpochMilli(endedAt).toLocalDate() else null,
            description = description,
            isVisible = isVisible,
            resume = resume,
        )

        return activityRepository.save(activity)
    }

    @Transactional
    fun updateActivity(
        id: String,
        type: ActivityType?,
        name: String?,
        organization: String?,
        certificateNumber: String?,
        startedAt: Long?,
        endedAt: Long?,
        description: String?,
        isVisible: Boolean?,
    ): Activity {
        val activity = this.getActivity(id)

        activity.changeInfo(
            type = type,
            name = name,
            organization = organization,
            certificateNumber = certificateNumber,
            startedAt = if (startedAt != null && startedAt > 0) TimeUtil.ofEpochMilli(startedAt).toLocalDate() else null,
            endedAt = if (endedAt != null && endedAt > 0) TimeUtil.ofEpochMilli(endedAt).toLocalDate() else null,
            description = description,
            isVisible = isVisible,
        )

        return activityRepository.save(activity)
    }

    @Transactional
    fun deleteActivity(id: String) {
        val activity = this.getActivity(id)
        activityRepository.delete(activity)
    }

    @Transactional
    fun deleteActivitiesByResumeId(resumeId: String) {
        val activities = activityRepository.findByResumeId(resumeId)
        activityRepository.deleteAll(activities)
    }
}
