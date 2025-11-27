package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.InterviewQuestion
import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.InterviewQuestionRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewQuestionService(
    private val interviewQuestionRepository: InterviewQuestionRepository,
) {
    @Transactional(readOnly = true)
    fun getInterviewQuestion(id: String): InterviewQuestion {
        return interviewQuestionRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_INTERVIEW_QUESTION.message)
        }
    }

    @Transactional(readOnly = true)
    fun getInterviewQuestions(turnOverId: String): List<InterviewQuestion> {
        return interviewQuestionRepository.findByTurnOverIdOrderByPriorityAsc(turnOverId)
    }

    // Cascade용: 엔티티만 생성 (저장 X)
    fun createEntity(
        turnOver: TurnOver,
        request: TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest,
    ): InterviewQuestion {
        return InterviewQuestion(
            question = request.question,
            answer = request.answer,
            turnOver = turnOver,
            isVisible = request.isVisible,
            priority = request.priority,
        )
    }

    @Transactional
    fun create(turnOver: TurnOver, request: TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest): InterviewQuestion {
        val interviewQuestion = createEntity(turnOver, request)
        return interviewQuestionRepository.save(interviewQuestion)
    }

    @Transactional
    fun createBulk(
        turnOver: TurnOver,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest>,
    ) {
        val entities = requests.map { request ->
            InterviewQuestion(
                question = request.question,
                answer = request.answer,
                turnOver = turnOver,
                isVisible = request.isVisible,
                priority = request.priority,
            )
        }

        interviewQuestionRepository.saveAll(entities)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest): InterviewQuestion {
        val interviewQuestion = this.getInterviewQuestion(request.id)

        interviewQuestion.changeInfo(
            question = request.question,
            answer = request.answer,
            isVisible = request.isVisible,
            priority = request.priority,
        )

        return interviewQuestionRepository.save(interviewQuestion)
    }

    @Transactional
    fun updateBulk(
        turnOverGoalId: String,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest>,
    ): List<InterviewQuestion> {
        val existingInterviewQuestions = this.getInterviewQuestions(turnOverGoalId)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingInterviewQuestions.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    question = request.question,
                    answer = request.answer,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return interviewQuestionRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteInterviewQuestions(ids: List<String>) {
        if (ids.isNotEmpty()) {
            interviewQuestionRepository.deleteAllById(ids)
        }
    }
}
