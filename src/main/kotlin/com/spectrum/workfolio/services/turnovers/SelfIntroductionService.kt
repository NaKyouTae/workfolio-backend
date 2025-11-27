package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.SelfIntroduction
import com.spectrum.workfolio.domain.entity.turnover.TurnOver
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
    fun getSelfIntroductions(turnOverId: String): List<SelfIntroduction> {
        return selfIntroductionRepository.findByTurnOverIdOrderByPriorityAsc(turnOverId)
    }

    // Cascade용: 엔티티만 생성 (저장 X)
    fun createEntity(
        turnOver: TurnOver,
        request: TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest,
    ): SelfIntroduction {
        return SelfIntroduction(
            question = request.question,
            content = request.content,
            turnOver = turnOver,
            isVisible = request.isVisible,
            priority = request.priority,
        )
    }

    @Transactional
    fun create(turnOver: TurnOver, request: TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest): SelfIntroduction {
        val selfIntroduction = createEntity(turnOver, request)
        return selfIntroductionRepository.save(selfIntroduction)
    }

    @Transactional
    fun createBulk(
        turnOver: TurnOver,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest>,
    ) {
        val entities = requests.map { request ->
            SelfIntroduction(
                question = request.question,
                content = request.content,
                turnOver = turnOver,
                isVisible = request.isVisible,
                priority = request.priority,
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
            isVisible = request.isVisible,
            priority = request.priority,
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
                    isVisible = request.isVisible,
                    priority = request.priority,
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
