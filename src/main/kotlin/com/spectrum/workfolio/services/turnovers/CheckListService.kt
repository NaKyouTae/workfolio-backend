package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.CheckList
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toWithoutTurnOverGoalProto
import com.spectrum.workfolio.domain.repository.CheckListRepository
import com.spectrum.workfolio.proto.turn_over.CheckListCheckedUpdateRequest
import com.spectrum.workfolio.proto.turn_over.CheckListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CheckListService(
    private val checkListRepository: CheckListRepository,
) {
    @Transactional(readOnly = true)
    fun getCheckList(id: String): CheckList {
        return checkListRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_CHECK_LIST.message)
        }
    }

    @Transactional(readOnly = true)
    fun getCheckLists(id: String): List<CheckList> {
        return checkListRepository.findByTurnOverGoalIdOrderByPriorityAsc(id)
    }

    // Cascade용: 엔티티만 생성 (저장 X)
    fun createEntity(
        turnOverGoal: TurnOverGoal,
        request: TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest,
    ): CheckList {
        return CheckList(
            checked = request.checked,
            content = request.content,
            turnOverGoal = turnOverGoal,
            isVisible = request.isVisible,
            priority = request.priority,
        )
    }

    @Transactional
    fun create(turnOverGoal: TurnOverGoal, request: TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest): CheckList {
        val checkList = createEntity(turnOverGoal, request)
        return checkListRepository.save(checkList)
    }

    @Transactional
    fun create(turnOverGoal: TurnOverGoal, requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest>) {
        val checkLists = requests.map {
            CheckList(
                checked = it.checked,
                content = it.content,
                turnOverGoal = turnOverGoal,
                isVisible = it.isVisible,
                priority = it.priority,
            )
        }

        checkListRepository.saveAll(checkLists)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest): CheckList {
        val checkList = this.getCheckList(request.id)

        checkList.changeInfo(
            checked = request.checked,
            content = request.content,
            isVisible = request.isVisible,
            priority = request.priority,
        )

        return checkListRepository.save(checkList)
    }

    @Transactional
    fun updateBulk(
        turnOverGoalId: String,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest>,
    ): List<CheckList> {
        val existingCheckLists = this.getCheckLists(turnOverGoalId)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingCheckLists.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    checked = request.checked,
                    content = request.content,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return checkListRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteCheckLists(ids: List<String>) {
        if (ids.isNotEmpty()) {
            checkListRepository.deleteAllById(ids)
        }
    }

    @Transactional
    fun updateChecked(request: CheckListCheckedUpdateRequest): CheckListResponse {
        val checkList = this.getCheckList(request.id)
        checkList.changeChecked(request.checked)
        val updatedCheckList = checkListRepository.save(checkList)
        return CheckListResponse.newBuilder()
            .setCheckList(updatedCheckList.toWithoutTurnOverGoalProto())
            .build()
    }
}
