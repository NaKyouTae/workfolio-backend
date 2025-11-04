package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.SelfIntroduction
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.SelfIntroductionRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SelfIntroductionService(
    private val selfIntroductionRepository: SelfIntroductionRepository,
) {
    @Transactional(readOnly = true)
    fun getSelfIntroduction(id: String): SelfIntroduction {
        return selfIntroductionRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_SELF_INTRODUCTION.message)
        }
    }

    @Transactional(readOnly = true)
    fun getSelfIntroductions(turnOverGoalId: String): List<SelfIntroduction> {
        return selfIntroductionRepository.findByTurnOverGoalId(turnOverGoalId)
    }

    // Cascade용: 엔티티만 생성 (저장 X)
    fun createEntity(
        turnOverGoal: TurnOverGoal,
        request: TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest,
    ): SelfIntroduction {
        return SelfIntroduction(
            question = request.question,
            content = request.content,
            turnOverGoal = turnOverGoal,
        )
    }

    @Transactional
    fun create(turnOverGoal: TurnOverGoal, request: TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest): SelfIntroduction {
        val selfIntroduction = createEntity(turnOverGoal, request)
        return selfIntroductionRepository.save(selfIntroduction)
    }

    @Transactional
    fun createBulk(
        turnOverGoal: TurnOverGoal,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest>,
    ) {
        val entities = requests.map { request ->
            SelfIntroduction(
                question = request.question,
                content = request.content,
                turnOverGoal = turnOverGoal,
            )
        }

        selfIntroductionRepository.saveAll(entities)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest): SelfIntroduction {
        val selfIntroduction = this.getSelfIntroduction(request.id)

        selfIntroduction.changeInfo(
            question = request.question,
            content = request.content,
        )

        return selfIntroductionRepository.save(selfIntroduction)
    }

    @Transactional
    fun updateBulk(
        turnOverGoalId: String,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest>,
    ): List<SelfIntroduction> {
        val existingSelfIntroductions = this.getSelfIntroductions(turnOverGoalId)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingSelfIntroductions.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    question = request.question,
                    content = request.content,
                )
                entity
            }
        }

        return selfIntroductionRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteSelfIntroductions(ids: List<String>) {
        if (ids.isNotEmpty()) {
            selfIntroductionRepository.deleteAllById(ids)
        }
    }
}
