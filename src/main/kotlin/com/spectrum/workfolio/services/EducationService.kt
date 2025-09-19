package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.primary.Education
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.EducationRepository
import com.spectrum.workfolio.proto.education.EducationCreateRequest
import com.spectrum.workfolio.proto.education.EducationListResponse
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
        val educations = educationRepository.findByWorkerId(worker.id)
        return EducationListResponse.newBuilder()
            .addAllEducations(educations.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createEducation(workerId: String, request: EducationCreateRequest): Education {
        val worker = workerService.getWorker(workerId)
        val education = Education(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            agency = request.agency,
            worker = worker,
        )

        return educationRepository.save(education)
    }

    @Transactional
    fun updateEducation(request: EducationUpdateRequest): Education {
        val education = this.getEducation(request.id)

        education.changeInfo(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            agency = request.agency,
        )

        return educationRepository.save(education)
    }
}
