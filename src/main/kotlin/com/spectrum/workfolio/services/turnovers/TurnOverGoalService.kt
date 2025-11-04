package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.TurnOverGoalRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TurnOverGoalService(
    private val turnOverGoalRepository: TurnOverGoalRepository,
) {
    @Transactional(readOnly = true)
    fun getTurnOverGoal(id: String): TurnOverGoal {
        return turnOverGoalRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_TURN_OVER_GOAL.message)
        }
    }

    @Transactional
    fun create(request: TurnOverUpsertRequest.TurnOverGoalRequest): TurnOverGoal {
        val turnOverGoal = TurnOverGoal(
            reason = request.reason,
            goal = request.goal,
        )

        return turnOverGoalRepository.save(turnOverGoal)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverGoalRequest): TurnOverGoal {
        val turnOverGoal = this.getTurnOverGoal(request.id)

        turnOverGoal.changeInfo(
            reason = request.reason,
            goal = request.goal,
        )

        return turnOverGoalRepository.save(turnOverGoal)
    }
}
