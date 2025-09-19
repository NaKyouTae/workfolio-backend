package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.history.Position
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.PositionRepository
import com.spectrum.workfolio.proto.position.PositionCreateRequest
import com.spectrum.workfolio.proto.position.PositionListResponse
import com.spectrum.workfolio.proto.position.PositionUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PositionService(
    private val companyService: CompanyService,
    private val positionRepository: PositionRepository,
) {

    @Transactional(readOnly = true)
    fun getPosition(id: String): Position {
        return positionRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_POSITION.message) }
    }

    @Transactional(readOnly = true)
    fun listPositions(companyId: String): PositionListResponse {
        val positions = positionRepository.findByCompanyId(companyId)
        return PositionListResponse.newBuilder()
            .addAllPositions(positions.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createPosition(request: PositionCreateRequest): Position {
        val company = companyService.getCompany(request.companyId)
        val position = Position(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            company = company,
        )

        return positionRepository.save(position)
    }

    @Transactional
    fun updatePosition(request: PositionUpdateRequest): Position {
        val position = this.getPosition(request.id)

        position.changeInfo(
            name = request.name,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
        )

        return positionRepository.save(position)
    }
}
