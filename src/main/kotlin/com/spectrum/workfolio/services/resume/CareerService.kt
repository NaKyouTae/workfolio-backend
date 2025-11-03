package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.Career
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.EmploymentType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.CareerRepository
import com.spectrum.workfolio.proto.career.CareerCreateRequest
import com.spectrum.workfolio.proto.career.CareerListResponse
import com.spectrum.workfolio.proto.career.CareerUpdateRequest
import com.spectrum.workfolio.utils.EnumUtils.convertProtoEnumSafe
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CareerService(
    private val careerRepository: CareerRepository,
    private val resumeQueryService: ResumeQueryService,
) {

    @Transactional(readOnly = true)
    fun getCareer(id: String): Career {
        return careerRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_CAREER.message) }
    }

    @Transactional(readOnly = true)
    fun listCareers(workerId: String): CareerListResponse {
        val careers = careerRepository.findByResumeIdOrderByPriorityAscStartedAtDescEndedAtDesc(workerId)
        return CareerListResponse.newBuilder()
            .addAllCareers(careers.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createCareer(request: CareerCreateRequest): Career {
        val resume = resumeQueryService.getResume(request.resumeId)
        val career = Career(
            name = request.name ?: "",
            position = request.position ?: "",
            employmentType = convertProtoEnumSafe<EmploymentType>(request.employmentType),
            department = request.department ?: "",
            jobTitle = request.jobTitle ?: "",
            rank = request.rank ?: "",
            salary = request.salary,
            description = request.description ?: "",
            startedAt = if (request.hasStartedAt() && request.startedAt != 0L) TimeUtil.ofEpochMilli(request.startedAt).toLocalDate() else null,
            endedAt = if (request.hasEndedAt() && request.endedAt != 0L) TimeUtil.ofEpochMilli(request.endedAt).toLocalDate() else null,
            isWorking = request.isWorking,
            isVisible = request.isVisible,
            priority = request.priority,
            resume = resume,
        )

        return careerRepository.save(career)
    }

    @Transactional
    fun createCareer(
        resume: Resume,
        career: Career,
    ): Career {
        val newCareer = Career(
            name = career.name,
            position = career.position,
            employmentType = career.employmentType,
            department = career.department,
            jobTitle = career.jobTitle,
            rank = career.rank,
            salary = career.salary,
            description = career.description,
            startedAt = career.startedAt,
            endedAt = career.endedAt,
            isWorking = career.isWorking,
            isVisible = career.isVisible,
            priority = career.priority,
            resume = resume,
        )

        return careerRepository.save(newCareer)
    }

    @Transactional
    fun updateCareer(request: CareerUpdateRequest): Career {
        val career = this.getCareer(request.id)

        career.changeInfo(
            name = request.name ?: "",
            position = request.position ?: "",
            employmentType = convertProtoEnumSafe<EmploymentType>(request.employmentType),
            department = request.department ?: "",
            jobTitle = request.jobTitle ?: "",
            rank = request.rank ?: "",
            salary = request.salary,
            description = request.description ?: "",
            startedAt = if (request.hasStartedAt() && request.startedAt != 0L) TimeUtil.ofEpochMilli(request.startedAt).toLocalDate() else null,
            endedAt = if (request.hasEndedAt() && request.endedAt != 0L) TimeUtil.ofEpochMilli(request.endedAt).toLocalDate() else null,
            isWorking = request.isWorking,
            isVisible = request.isVisible,
            priority = request.priority,
        )

        return careerRepository.save(career)
    }

    @Transactional
    fun deleteCareer(id: String) {
        val career = this.getCareer(id)
        careerRepository.delete(career)
    }

    @Transactional
    fun deleteCareers(careerIds: List<String>) {
        if (careerIds.isNotEmpty()) {
            careerRepository.deleteAllById(careerIds)
        }
    }

    @Transactional
    fun deleteCareersByResumeId(resumeId: String) {
        val careers = careerRepository.findByResumeIdOrderByPriorityAscStartedAtDescEndedAtDesc(resumeId)
        careerRepository.deleteAll(careers)
    }
}
