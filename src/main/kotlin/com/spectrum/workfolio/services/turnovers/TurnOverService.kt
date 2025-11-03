package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.repository.TurnOverRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TurnOverService(
    private val turnOverRepository: TurnOverRepository,
) {

    @Transactional(readOnly = true)
    fun listTurnOversResult(workerId: String): TurnOverDetailListResponse {
        val turnOvers = turnOverRepository.findByWorkerId(workerId)
        return TurnOverDetailListResponse.newBuilder()
            .addAllTurnOvers(turnOvers.map { it.toDetailProto() })
            .build()
    }

    @Transactional
    fun upsertTurnOver(workerId: String, request: TurnOverUpsertRequest) {
//        upsertTurnOver()
//        upsertTurnOverGoal()
//        upsertTurnOverChallenge()
//        upsertTurnOverRetrospective()
    }
}
