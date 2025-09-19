package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.primary.Degrees
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
    private val workerService: WorkerService,
    private val degreesRepository: DegreesRepository,
) {

    @Transactional(readOnly = true)
    fun getDegrees(id: String): Degrees {
        return degreesRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_DEGREES.message) }
    }

    @Transactional(readOnly = true)
    fun listDegrees(workerId: String): DegreesListResponse {
        val worker = workerService.getWorker(workerId)
        val degrees = degreesRepository.findByWorkerIdOrderByStartedAtDescEndedAtDesc(worker.id)
        return DegreesListResponse.newBuilder()
            .addAllDegrees(degrees.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createDegrees(workerId: String, request: DegreesCreateRequest): DegreesResponse {
        val worker = workerService.getWorker(workerId)
        val degrees = Degrees(
            name = request.name,
            major = request.major,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            worker = worker,
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
