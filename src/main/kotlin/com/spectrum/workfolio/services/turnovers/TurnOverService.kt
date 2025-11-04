package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import com.spectrum.workfolio.domain.enums.ApplicationStageStatus
import com.spectrum.workfolio.domain.enums.AttachmentTargetType
import com.spectrum.workfolio.domain.enums.MemoTargetType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.TurnOverRepository
import com.spectrum.workfolio.proto.attachment.AttachmentRequest
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.services.AttachmentCommandService
import com.spectrum.workfolio.services.AttachmentQueryService
import com.spectrum.workfolio.services.MemoCommandService
import com.spectrum.workfolio.services.MemoQueryService
import com.spectrum.workfolio.services.WorkerService
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TurnOverService(
    private val workerService: WorkerService,
    private val checkListService: CheckListService,
    private val memoQueryService: MemoQueryService,
    private val memoCommandService: MemoCommandService,
    private val turnOverRepository: TurnOverRepository,
    private val turnOverGoalService: TurnOverGoalService,
    private val jobApplicationService: JobApplicationService,
    private val attachmentQueryService: AttachmentQueryService,
    private val applicationStageService: ApplicationStageService,
    private val selfIntroductionService: SelfIntroductionService,
    private val attachmentCommandService: AttachmentCommandService,
    private val interviewQuestionService: InterviewQuestionService,
    private val turnOverChallengeService: TurnOverChallengeService,
    private val turnOverRetrospectiveService: TurnOverRetrospectiveService,
) {

    @Transactional(readOnly = true)
    fun getTurnOver(id: String): TurnOver {
        return turnOverRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_TURN_OVER.message)
        }
    }

    @Transactional(readOnly = true)
    fun getTurnOverDetailResult(id: String): TurnOverDetailResponse {
        val turnOver = this.getTurnOver(id)

        val turnOverGoal = turnOverGoalService.getTurnOverGoalDetail(turnOver.turnOverGoal.id)
        val turnOverChallenge = turnOverChallengeService.getTurnOverChallengeDetail(
            turnOver.turnOverChallenge.id,
        )
        val turnOverRetrospective = turnOverRetrospectiveService.getTurnOverRetrospectiveDetail(
            turnOver.turnOverRetrospective.id,
        )

        return TurnOverDetailResponse.newBuilder()
            .setTurnOver(
                turnOver.toDetailProto(
                    turnOverGoal = turnOverGoal,
                    turnOverChallenge = turnOverChallenge,
                    turnOverRetrospective = turnOverRetrospective,
                ),
            )
            .build()
    }

    @Transactional(readOnly = true)
    fun listTurnOversResult(workerId: String): TurnOverListResponse {
        val turnOvers = turnOverRepository.findByWorkerId(workerId)
        return TurnOverListResponse.newBuilder()
            .addAllTurnOvers(turnOvers.map { it.toProto() })
            .build()
    }

    @Transactional
    fun upsertTurnOver(workerId: String, request: TurnOverUpsertRequest) {
        val turnOverGoal = upsertTurnOverGoal(request.turnOverGoal)
        val turnOverChallenge = upsertTurnOverChallenge(request.turnOverChallenge)
        val turnOverRetrospective = upsertTurnOverRetrospective(request.turnOverRetrospective)
        val worker = workerService.getWorker(workerId)

        val turnOverEntity = if (request.hasId()) {
            val turnOver = this.getTurnOver(request.id)

            turnOver.changeInfo(
                name = request.name,
            )

            turnOver
        } else {
            TurnOver(
                name = request.name,
                turnOverGoal = turnOverGoal,
                turnOverChallenge = turnOverChallenge,
                turnOverRetrospective = turnOverRetrospective,
                worker = worker,
            )
        }

        turnOverRepository.save(turnOverEntity)
    }

    private fun upsertTurnOverGoal(request: TurnOverUpsertRequest.TurnOverGoalRequest): TurnOverGoal {
        val turnOverGoal = if (request.hasId()) {
            turnOverGoalService.update(request)
        } else {
            turnOverGoalService.create(request)
        }

        upsertSelfIntroduction(turnOverGoal, request.selfIntroductionsList)
        upsertInterviewQuestion(turnOverGoal, request.interviewQuestionsList)
        upsertCheckList(turnOverGoal, request.checkListList)
        upsertMemo(MemoTargetType.TURN_OVER_GOAL, turnOverGoal.id, request.memosList)
        updateAttachments(turnOverGoal.id, request.attachmentsList)

        return turnOverGoal
    }

    private fun upsertSelfIntroduction(
        turnOverGoal: TurnOverGoal,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOverGoal.selfIntroductions.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existingMap[request.id]!!.apply {
                    changeInfo(
                        question = request.question,
                        content = request.content,
                    )
                }
            } else {
                // 새 엔티티 생성
                selfIntroductionService.createEntity(turnOverGoal, request)
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOverGoal.syncSelfIntroductions(updatedEntities)
    }

    private fun upsertInterviewQuestion(
        turnOverGoal: TurnOverGoal,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOverGoal.interviewQuestions.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existingMap[request.id]!!.apply {
                    changeInfo(
                        question = request.question,
                        answer = request.answer,
                    )
                }
            } else {
                // 새 엔티티 생성
                interviewQuestionService.createEntity(turnOverGoal, request)
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOverGoal.syncInterviewQuestions(updatedEntities)
    }

    private fun upsertCheckList(
        turnOverGoal: TurnOverGoal,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOverGoal.checkList.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existingMap[request.id]!!.apply {
                    changeInfo(
                        checked = request.checked,
                        content = request.content,
                    )
                }
            } else {
                // 새 엔티티 생성
                checkListService.createEntity(turnOverGoal, request)
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOverGoal.syncCheckLists(updatedEntities)
    }

    private fun upsertTurnOverChallenge(request: TurnOverUpsertRequest.TurnOverChallengeRequest): TurnOverChallenge {
        val turnOverChallenge = if (request.hasId()) {
            turnOverChallengeService.update(request)
        } else {
            turnOverChallengeService.create(request)
        }

        upsertJobApplication(turnOverChallenge, request.jobApplicationsList)
        upsertMemo(MemoTargetType.TURN_OVER_CHALLENGE, turnOverChallenge.id, request.memosList)
        updateAttachments(turnOverChallenge.id, request.attachmentsList)

        return turnOverChallenge
    }

    private fun upsertJobApplication(
        turnOverChallenge: TurnOverChallenge,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOverChallenge.jobApplications.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                val jobApplication = existingMap[request.id]!!.apply {
                    changeInfo(
                        name = request.name,
                        position = request.position,
                        jobPostingTitle = request.jobPostingTitle,
                        jobPostingUrl = request.jobPostingUrl,
                        startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                        endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                        applicationSource = request.applicationSource,
                        memo = request.memo,
                    )
                }
                // ApplicationStage도 Cascade로 처리
                upsertApplicationStage(jobApplication, request.applicationStagesList)
                jobApplication
            } else {
                // 새 엔티티 생성
                val jobApplication = jobApplicationService.createEntity(turnOverChallenge, request)
                // ApplicationStage도 Cascade로 처리
                upsertApplicationStage(jobApplication, request.applicationStagesList)
                jobApplication
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOverChallenge.syncJobApplications(updatedEntities)
    }

    private fun upsertApplicationStage(
        jobApplication: JobApplication,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = jobApplication.applicationStages.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existingMap[request.id]!!.apply {
                    changeInfo(
                        name = request.name,
                        status = ApplicationStageStatus.valueOf(request.status.name),
                        startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                        memo = request.memo,
                    )
                }
            } else {
                // 새 엔티티 생성
                applicationStageService.createEntity(jobApplication, request)
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        jobApplication.syncApplicationStages(updatedEntities)
    }

    private fun upsertTurnOverRetrospective(request: TurnOverUpsertRequest.TurnOverRetrospectiveRequest): TurnOverRetrospective {
        val turnOverRetrospective = if (request.hasId()) {
            turnOverRetrospectiveService.update(request)
        } else {
            turnOverRetrospectiveService.create(request)
        }

        upsertMemo(MemoTargetType.TURN_OVER_RETROSPECT, turnOverRetrospective.id, request.memosList)
        updateAttachments(turnOverRetrospective.id, request.attachmentsList)

        return turnOverRetrospective
    }

    private fun upsertMemo(
        targetType: MemoTargetType,
        targetId: String,
        requests: List<TurnOverUpsertRequest.MemoRequest>,
    ) {
        val existingEntities = memoQueryService.listMemos(targetId)
        val existingIds = existingEntities.map { it.id }.toSet()
        val requestIds = requests.mapNotNull { it.id }.toSet()

        val toDelete = existingEntities.filter { it.id !in requestIds }
        val createMemos = requests.filter { !it.hasId() }
        val updateMemos = requests.filter { it.hasId() }

        memoCommandService.deleteMemos(toDelete.map { it.id })
        memoCommandService.createBulkMemo(targetType, targetId, createMemos)
        memoCommandService.updateBulkMemo(targetId, updateMemos)
    }

    private fun updateAttachments(
        targetId: String,
        attachmentRequests: List<AttachmentRequest>,
    ) {
        val existingAttachments = attachmentQueryService.listAttachments(targetId)
        val existingIds = existingAttachments.map { it.id }.toSet()
        val requestIds = attachmentRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingAttachments.filter { it.id !in requestIds }
        val createRequests = attachmentRequests.filter { !it.hasId() }
        val updateRequests = attachmentRequests.filter { it.hasId() }

        attachmentCommandService.deleteAttachments(toDelete.map { it.id })
        attachmentCommandService.createBulkAttachment(AttachmentTargetType.ENTITY_RESUME, targetId, createRequests)
        attachmentCommandService.updateBulkAttachment(targetId, updateRequests)
    }

    @Transactional
    fun duplicate(id: String) {
        // TODO duplicate turn over
    }

    @Transactional
    fun delete(id: String) {
        turnOverRepository.deleteById(id)
    }
}
