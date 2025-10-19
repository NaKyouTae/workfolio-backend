package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Education
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
    private val resumeService: ResumeService,
    private val educationRepository: EducationRepository,
) {

    @Transactional(readOnly = true)
    fun getEducation(id: String): Education {
        return educationRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_EDUCATION.message) }
    }

    @Transactional(readOnly = true)
    fun listEducations(resumeId: String): EducationListResponse {
        val resume = resumeService.getResume(resumeId)
        val educations = educationRepository.findByResumeIdOrderByStartedAtDescEndedAtDesc(resume.id)
        return EducationListResponse.newBuilder()
            .addAllEducations(educations.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createEducation(request: EducationCreateRequest): EducationResponse {
        val resume = resumeService.getResume(request.resumeId)
        val education = Education(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            agency = request.agency,
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
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            agency = request.agency,
        )

        val updatedEducation = educationRepository.save(education)

        return EducationResponse.newBuilder().setEducation(updatedEducation.toProto()).build()
    }
}
