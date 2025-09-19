package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.primary.Education
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
    private val workerService: WorkerService,
    private val educationRepository: EducationRepository,
) {

    @Transactional(readOnly = true)
    fun getEducation(id: String): Education {
        return educationRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_EDUCATION.message) }
    }

    @Transactional(readOnly = true)
    fun listEducations(workerId: String): EducationListResponse {
        val worker = workerService.getWorker(workerId)
        val educations = educationRepository.findByWorkerIdOrderByStartedAtDescEndedAtDesc(worker.id)
        return EducationListResponse.newBuilder()
            .addAllEducations(educations.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createEducation(workerId: String, request: EducationCreateRequest): EducationResponse {
        val worker = workerService.getWorker(workerId)
        val education = Education(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            agency = request.agency,
            worker = worker,
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
