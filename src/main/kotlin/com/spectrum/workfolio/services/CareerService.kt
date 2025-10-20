package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Career
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.CareerRepository
import com.spectrum.workfolio.proto.career.CareerCreateRequest
import com.spectrum.workfolio.proto.career.CareerListResponse
import com.spectrum.workfolio.proto.career.CareerResponse
import com.spectrum.workfolio.proto.career.CareerUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CareerService(
    private val resumeService: ResumeService,
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
    fun createCareer(request: CareerCreateRequest): CareerResponse {
        val resume = resumeService.getResume(request.resumeId)
        val career = Career(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            isWorking = request.isWorking,
            resume = resume,
        )

        val createdCareer = careerRepository.save(career)

        return CareerResponse.newBuilder().setCareer(createdCareer.toProto()).build()
    }

    @Transactional
    fun updateCareer(request: CareerUpdateRequest): CareerResponse {
        val career = this.getCareer(request.id)

        career.changeInfo(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            isWorking = request.isWorking,
        )

        val createdCareer = careerRepository.save(career)

        return CareerResponse.newBuilder().setCareer(createdCareer.toProto()).build()
    }
}
