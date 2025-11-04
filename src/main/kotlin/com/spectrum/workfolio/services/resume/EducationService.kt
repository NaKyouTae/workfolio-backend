package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.Education
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.EducationStatus
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.EducationRepository
import com.spectrum.workfolio.proto.education.EducationCreateRequest
import com.spectrum.workfolio.proto.education.EducationListResponse
import com.spectrum.workfolio.proto.education.EducationResponse
import com.spectrum.workfolio.proto.education.EducationUpdateRequest
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
import com.spectrum.workfolio.utils.EnumUtils.convertProtoEnumSafe
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EducationService(
    private val resumeQueryService: ResumeQueryService,
    private val educationRepository: EducationRepository,
) {

    @Transactional(readOnly = true)
    fun getEducation(id: String): Education {
        return educationRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_EDUCATION.message) }
    }

    @Transactional(readOnly = true)
    fun listEducations(resumeId: String): EducationListResponse {
        val resume = resumeQueryService.getResume(resumeId)
        val educations = educationRepository.findByResumeIdOrderByPriorityAscStartedAtDescEndedAtDesc(resume.id)
        return EducationListResponse.newBuilder()
            .addAllEducations(educations.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createEducation(request: EducationCreateRequest): EducationResponse {
        val resume = resumeQueryService.getResume(request.resumeId)
        val education = Education(
            name = request.name,
            major = request.major,
            description = request.description,
            status = convertProtoEnumSafe<EducationStatus>(request.status),
            startedAt = if (request.hasStartedAt() && request.startedAt != 0L) TimeUtil.ofEpochMilli(request.startedAt).toLocalDate() else null,
            endedAt = if (request.hasEndedAt() && request.endedAt != 0L) TimeUtil.ofEpochMilli(request.endedAt).toLocalDate() else null,
            isVisible = request.isVisible,
            priority = request.priority,
            resume = resume,
        )

        val createdEducation = educationRepository.save(education)

        return EducationResponse.newBuilder().setEducation(createdEducation.toProto()).build()
    }

    @Transactional
    fun createBulkEducation(
        resume: Resume,
        educations: List<Education>,
    ) {
        val newEducations = educations.map {
            Education(
                name = it.name,
                major = it.major,
                description = it.description,
                status = it.status,
                startedAt = it.startedAt,
                endedAt = it.endedAt,
                isVisible = it.isVisible,
                priority = it.priority,
                resume = resume,
            )
        }

        educationRepository.saveAll(newEducations)
    }

    @Transactional
    fun createBulkEducation(
        resumeId: String,
        requests: List<ResumeUpdateRequest.EducationRequest>,
    ) {
        val resume = resumeQueryService.getResume(resumeId)
        val entities = requests.map { request ->
            Education(
                name = request.name,
                major = request.major,
                description = request.description,
                status = convertProtoEnumSafe<EducationStatus>(request.status),
                startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                isVisible = request.isVisible,
                priority = request.priority,
                resume = resume,
            )
        }

        educationRepository.saveAll(entities)
    }

    @Transactional
    fun updateEducation(request: EducationUpdateRequest): EducationResponse {
        val education = this.getEducation(request.id)

        education.changeInfo(
            name = request.name,
            major = request.major,
            description = request.description,
            status = convertProtoEnumSafe<EducationStatus>(request.status),
            startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            isVisible = request.isVisible,
            priority = request.priority,
        )

        val updatedEducation = educationRepository.save(education)

        return EducationResponse.newBuilder().setEducation(updatedEducation.toProto()).build()
    }

    @Transactional
    fun updateBulkEducation(
        resumeId: String,
        requests: List<ResumeUpdateRequest.EducationRequest>,
    ): List<Education> {
        val existingEducations = educationRepository.findByResumeIdOrderByPriorityAscStartedAtDescEndedAtDesc(resumeId)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingEducations.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    name = request.name,
                    major = request.major,
                    description = request.description,
                    status = convertProtoEnumSafe<EducationStatus>(request.status),
                    startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                    endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return educationRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteEducation(id: String) {
        val education = this.getEducation(id)
        educationRepository.delete(education)
    }

    @Transactional
    fun deleteEducations(educationIds: List<String>) {
        if (educationIds.isNotEmpty()) {
            educationRepository.deleteAllById(educationIds)
        }
    }

    @Transactional
    fun deleteEducationsByResumeId(resumeId: String) {
        val educations = educationRepository.findByResumeIdOrderByPriorityAscStartedAtDescEndedAtDesc(resumeId)
        educationRepository.deleteAll(educations)
    }
}
