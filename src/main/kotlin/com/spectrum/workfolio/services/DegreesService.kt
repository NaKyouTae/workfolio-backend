package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Degrees
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.DegreesRepository
import com.spectrum.workfolio.proto.degrees.DegreesCreateRequest
import com.spectrum.workfolio.proto.degrees.DegreesListResponse
import com.spectrum.workfolio.proto.degrees.DegreesResponse
import com.spectrum.workfolio.proto.degrees.DegreesUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DegreesService(
    private val resumeService: ResumeService,
    private val degreesRepository: DegreesRepository,
) {

    @Transactional(readOnly = true)
    fun getDegrees(id: String): Degrees {
        return degreesRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_DEGREES.message) }
    }

    @Transactional(readOnly = true)
    fun listDegrees(resumeId: String): DegreesListResponse {
        val resume = resumeService.getResume(resumeId)
        val degrees = degreesRepository.findByResumeIdOrderByStartedAtDescEndedAtDesc(resume.id)
        return DegreesListResponse.newBuilder()
            .addAllDegrees(degrees.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createDegrees(request: DegreesCreateRequest): DegreesResponse {
        val resume = resumeService.getResume(request.resumeId)
        val degrees = Degrees(
            name = request.name,
            major = request.major,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            resume = resume,
        )

        val createdDegrees = degreesRepository.save(degrees)

        return DegreesResponse.newBuilder().setDegrees(createdDegrees.toProto()).build()
    }

    @Transactional
    fun updateDegrees(request: DegreesUpdateRequest): DegreesResponse {
        val degrees = this.getDegrees(request.id)

        degrees.changeInfo(
            name = request.name,
            major = request.major,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
        )

        val updatedDegrees = degreesRepository.save(degrees)

        return DegreesResponse.newBuilder().setDegrees(updatedDegrees.toProto()).build()
    }
}
