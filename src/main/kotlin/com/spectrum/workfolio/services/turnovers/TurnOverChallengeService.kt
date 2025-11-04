package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.TurnOverChallengeRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TurnOverChallengeService(
    private val turnOverChallengeRepository: TurnOverChallengeRepository,
) {
    @Transactional(readOnly = true)
    fun getTurnOverChallenge(id: String): TurnOverChallenge {
        return turnOverChallengeRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_TURN_OVER_CHALLENGE.message)
        }
    }

    @Transactional
    fun create(request: TurnOverUpsertRequest.TurnOverChallengeRequest): TurnOverChallenge {
        val turnOverChallenge = TurnOverChallenge()

        return turnOverChallengeRepository.save(turnOverChallenge)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverChallengeRequest): TurnOverChallenge {
        val turnOverChallenge = this.getTurnOverChallenge(request.id)

        return turnOverChallengeRepository.save(turnOverChallenge)
    }
}
