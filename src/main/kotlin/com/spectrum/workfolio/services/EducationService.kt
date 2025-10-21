package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Education
import com.spectrum.workfolio.domain.enums.EducationStatus
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.EducationRepository
import com.spectrum.workfolio.proto.education.EducationCreateRequest
import com.spectrum.workfolio.proto.education.EducationListResponse
import com.spectrum.workfolio.proto.education.EducationResponse
import com.spectrum.workfolio.proto.education.EducationUpdateRequest
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
        val educations = educationRepository.findByResumeIdOrderByStartedAtDescEndedAtDesc(resume.id)
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
            status = EducationStatus.valueOf(request.status.name),
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            resume = resume,
        )

        val createdEducation = educationRepository.save(education)

        return EducationResponse.newBuilder().setEducation(createdEducation.toProto()).build()
    }

    @Transactional
    fun updateEducation(request: EducationUpdateRequest): EducationResponse {
        val education = this.getEducation(request.id)

        education.changeInfo(
            name = request.name,
            major = request.major,
            status = EducationStatus.valueOf(request.status.name),
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
        )

        val updatedEducation = educationRepository.save(education)

        return EducationResponse.newBuilder().setEducation(updatedEducation.toProto()).build()
    }

    @Transactional
    fun deleteEducation(id: String) {
        val education = this.getEducation(id)
        educationRepository.delete(education)
    }

    @Transactional
    fun deleteEducationsByResumeId(resumeId: String) {
        val educations = educationRepository.findByResumeIdOrderByStartedAtDescEndedAtDesc(resumeId)
        educationRepository.deleteAll(educations)
    }
}
