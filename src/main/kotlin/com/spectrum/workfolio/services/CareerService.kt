package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Career
import com.spectrum.workfolio.domain.enums.EmploymentType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.CareerRepository
import com.spectrum.workfolio.proto.career.CareerCreateRequest
import com.spectrum.workfolio.proto.career.CareerListResponse
import com.spectrum.workfolio.proto.career.CareerUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CareerService(
    private val resumeQueryService: ResumeQueryService,
    private val careerRepository: CareerRepository,
) {

    @Transactional(readOnly = true)
    fun getCareer(id: String): Career {
        return careerRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_CAREER.message) }
    }

    @Transactional(readOnly = true)
    fun listCareers(workerId: String): CareerListResponse {
        val careers = careerRepository.findByResumeIdOrderByStartedAtDescEndedAtDesc(workerId)
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
            employmentType = if (request.hasEmploymentType()) EmploymentType.valueOf(request.employmentType.name) else null,
            department = request.department ?: "",
            jobGrade = request.jobGrade ?: "",
            job = request.job ?: "",
            salary = request.salary,
            description = request.description ?: "",
            startedAt = if (request.hasStartedAt() && request.startedAt != 0L) TimeUtil.ofEpochMilli(request.startedAt).toLocalDate() else null,
            endedAt = if (request.hasEndedAt() && request.endedAt != 0L) TimeUtil.ofEpochMilli(request.endedAt).toLocalDate() else null,
            isWorking = request.isWorking,
            isVisible = request.isVisible,
            resume = resume,
        )

        return careerRepository.save(career)
    }

    @Transactional
    fun updateCareer(request: CareerUpdateRequest): Career {
        val career = this.getCareer(request.id)

        career.changeInfo(
            name = request.name ?: "",
            position = request.position ?: "",
            employmentType = if (request.hasEmploymentType()) EmploymentType.valueOf(request.employmentType.name) else null,
            department = request.department ?: "",
            jobGrade = request.jobGrade ?: "",
            job = request.job ?: "",
            salary = request.salary,
            description = request.description ?: "",
            startedAt = if (request.hasStartedAt() && request.startedAt != 0L) TimeUtil.ofEpochMilli(request.startedAt).toLocalDate() else null,
            endedAt = if (request.hasEndedAt() && request.endedAt != 0L) TimeUtil.ofEpochMilli(request.endedAt).toLocalDate() else null,
            isWorking = request.isWorking,
            isVisible = request.isVisible,
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
        val careers = careerRepository.findByResumeIdOrderByStartedAtDescEndedAtDesc(resumeId)
        careerRepository.deleteAll(careers)
    }
}
