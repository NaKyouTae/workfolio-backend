package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import com.spectrum.workfolio.domain.enums.ApplicationStageStatus
import com.spectrum.workfolio.domain.enums.AttachmentTargetType
import com.spectrum.workfolio.domain.enums.EmploymentType
import com.spectrum.workfolio.domain.enums.JobApplicationStatus
import com.spectrum.workfolio.domain.enums.MemoTargetType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.TurnOverRepository
import com.spectrum.workfolio.domain.repository.SelfIntroductionRepository
import com.spectrum.workfolio.domain.repository.InterviewQuestionRepository
import com.spectrum.workfolio.domain.repository.CheckListRepository
import com.spectrum.workfolio.domain.repository.JobApplicationRepository
import com.spectrum.workfolio.proto.attachment.AttachmentRequest
import com.spectrum.workfolio.proto.common.Attachment
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.services.AttachmentCommandService
import com.spectrum.workfolio.services.AttachmentQueryService
import com.spectrum.workfolio.services.FileUploadService
import com.spectrum.workfolio.services.MemoCommandService
import com.spectrum.workfolio.services.MemoQueryService
import com.spectrum.workfolio.services.WorkerService
import com.spectrum.workfolio.utils.EnumUtils.convertProtoEnumSafe
import com.spectrum.workfolio.utils.FileUtil
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
    private val fileUploadService: FileUploadService,
    private val turnOverRepository: TurnOverRepository,
    private val attachmentRepository: com.spectrum.workfolio.domain.repository.AttachmentRepository,
    private val jobApplicationService: JobApplicationService,
    private val attachmentQueryService: AttachmentQueryService,
    private val applicationStageService: ApplicationStageService,
    private val selfIntroductionService: SelfIntroductionService,
    private val attachmentCommandService: AttachmentCommandService,
    private val interviewQuestionService: InterviewQuestionService,
    private val selfIntroductionRepository: SelfIntroductionRepository,
    private val interviewQuestionRepository: InterviewQuestionRepository,
    private val checkListRepository: CheckListRepository,
    private val jobApplicationRepository: JobApplicationRepository,
) {

    private val logger = org.slf4j.LoggerFactory.getLogger(TurnOverService::class.java)

    @Transactional(readOnly = true)
    fun getTurnOver(id: String): TurnOver {
        return turnOverRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_TURN_OVER.message)
        }
    }

    @Transactional(readOnly = true)
    fun getTurnOverDetailResult(id: String): TurnOverDetailResponse {
        val turnOver = this.getTurnOver(id)
        val allMemos = memoQueryService.listMemos(turnOver.id)
        val allAttachments = attachmentQueryService.listAttachments(turnOver.id)

        val memosGoal = allMemos.filter { it.targetType == MemoTargetType.TURN_OVER_GOAL }
        val attachmentsGoal = allAttachments.filter { it.targetType == AttachmentTargetType.ENTITY_TURN_OVER_GOAL }
        val turnOverGoal = turnOver.turnOverGoal.toDetailProto(
            turnOver.selfIntroductions,
            turnOver.interviewQuestions,
            turnOver.checkList,
            memosGoal,
            attachmentsGoal,
            turnOver.id,
        )

        val memosChallenge = allMemos.filter { it.targetType == MemoTargetType.TURN_OVER_CHALLENGE }
        val attachmentsChallenge = allAttachments.filter { it.targetType == AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE }
        val turnOverChallenge = (turnOver.turnOverChallenge ?: TurnOverChallenge()).toDetailProto(
            turnOver.jobApplications,
            memosChallenge,
            attachmentsChallenge,
            turnOver.id,
        )

        val memosRetrospective = allMemos.filter { it.targetType == MemoTargetType.TURN_OVER_RETROSPECT }
        val attachmentsRetrospective = allAttachments.filter { it.targetType == AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE }
        val turnOverRetrospective =
            turnOver.turnOverRetrospective.toDetailProto(memosRetrospective, attachmentsRetrospective, turnOver.id)

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

    @Transactional(readOnly = true)
    fun listTurnOverDetailsResult(workerId: String): TurnOverDetailListResponse {
        // MultipleBagFetchException 방지: JOIN FETCH 없이 기본 조회만 수행
        val turnOvers = turnOverRepository.findByWorkerIdWithCollections(workerId)
        
        // N+1 문제 해결: 모든 turnOver ID를 모아서 한 번에 조회
        val turnOverIds = turnOvers.map { it.id }
        
        // 컬렉션들을 별도 쿼리로 한 번에 가져오기
        val allSelfIntroductionsMap = selfIntroductionRepository
            .findByTurnOverIdInOrderByTurnOverIdAscPriorityAsc(turnOverIds)
            .groupBy { it.turnOver.id }
        val allInterviewQuestionsMap = interviewQuestionRepository
            .findByTurnOverIdInOrderByTurnOverIdAscPriorityAsc(turnOverIds)
            .groupBy { it.turnOver.id }
        val allCheckListMap = checkListRepository
            .findByTurnOverIdInOrderByTurnOverIdAscPriorityAsc(turnOverIds)
            .groupBy { it.turnOver.id }
        val allJobApplicationsMap = jobApplicationRepository
            .findByTurnOverIdInOrderByTurnOverIdAscPriorityAsc(turnOverIds)
            .groupBy { it.turnOver.id }
        val allMemosMap = memoQueryService.listMemos(turnOverIds).groupBy { it.targetId }
        val allAttachmentsMap = attachmentQueryService.listAttachments(turnOverIds).groupBy { it.targetId }
        
        val turnOversResult = turnOvers.map {
            val selfIntroductions = allSelfIntroductionsMap[it.id] ?: emptyList()
            val interviewQuestions = allInterviewQuestionsMap[it.id] ?: emptyList()
            val checkList = allCheckListMap[it.id] ?: emptyList()
            val jobApplications = allJobApplicationsMap[it.id] ?: emptyList()
            val allMemos = allMemosMap[it.id] ?: emptyList()
            val allAttachments = allAttachmentsMap[it.id] ?: emptyList()

            val memosGoal = allMemos.filter { memo -> memo.targetType == MemoTargetType.TURN_OVER_GOAL }
            val attachmentsGoal = allAttachments.filter { attachment -> attachment.targetType == AttachmentTargetType.ENTITY_TURN_OVER_GOAL }
            val turnOverGoal = it.turnOverGoal.toDetailProto(
                selfIntroductions,
                interviewQuestions,
                checkList,
                memosGoal,
                attachmentsGoal,
                it.id,
            )

            val memosChallenge = allMemos.filter { memo -> memo.targetType == MemoTargetType.TURN_OVER_CHALLENGE }
            val attachmentsChallenge = allAttachments.filter { attachment -> attachment.targetType == AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE }
            val turnOverChallenge = (it.turnOverChallenge ?: TurnOverChallenge()).toDetailProto(
                jobApplications,
                memosChallenge,
                attachmentsChallenge,
                it.id,
            )

            val memosRetrospective = allMemos.filter { memo -> memo.targetType == MemoTargetType.TURN_OVER_RETROSPECT }
            val attachmentsRetrospective = allAttachments.filter { attachment -> attachment.targetType == AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE }
            val turnOverRetrospective =
                it.turnOverRetrospective.toDetailProto(memosRetrospective, attachmentsRetrospective, it.id)

            it.toDetailProto(turnOverGoal, turnOverChallenge, turnOverRetrospective)
        }

        return TurnOverDetailListResponse.newBuilder()
            .addAllTurnOvers(turnOversResult)
            .build()
    }

    @Transactional(timeout = 60)  // 복잡한 작업이므로 더 긴 타임아웃 필요
    fun upsertTurnOver(workerId: String, request: TurnOverUpsertRequest) {
        val worker = workerService.getWorker(workerId)
        val turnOver = this.getTurnOver(request.id)

        val turnOverEntity = if (request.hasId()) {
            turnOver.changeInfo(
                name = request.name,
                startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            )

            // Update embedded objects
            turnOver.turnOverGoal.changeInfo(
                reason = request.turnOverGoal.reason,
                goal = request.turnOverGoal.goal,
            )

            turnOver.turnOverRetrospective.changeInfo(
                name = request.turnOverRetrospective.name,
                salary = request.turnOverRetrospective.salary,
                position = request.turnOverRetrospective.position,
                jobTitle = request.turnOverRetrospective.jobTitle,
                rank = request.turnOverRetrospective.rank,
                department = request.turnOverRetrospective.department,
                reason = request.turnOverRetrospective.reason,
                score = request.turnOverRetrospective.score,
                reviewSummary = request.turnOverRetrospective.reviewSummary,
                joinedAt = TimeUtil.ofEpochMilliNullable(request.turnOverRetrospective.joinedAt)?.toLocalDate(),
                workType = request.turnOverRetrospective.workType,
                employmentType = convertProtoEnumSafe<EmploymentType>(request.turnOverRetrospective.employmentType),
            )

            // Upsert collections
            upsertSelfIntroduction(turnOver, request.turnOverGoal.selfIntroductionsList)
            upsertInterviewQuestion(turnOver, request.turnOverGoal.interviewQuestionsList)
            upsertCheckList(turnOver, request.turnOverGoal.checkListList)
            upsertJobApplication(turnOver, request.turnOverChallenge.jobApplicationsList)
            upsertMemo(MemoTargetType.TURN_OVER_GOAL, turnOver.id, request.turnOverGoal.memosList)
            upsertMemo(MemoTargetType.TURN_OVER_CHALLENGE, turnOver.id, request.turnOverChallenge.memosList)
            upsertMemo(MemoTargetType.TURN_OVER_RETROSPECT, turnOver.id, request.turnOverRetrospective.memosList)
            updateAttachments(
                AttachmentTargetType.ENTITY_TURN_OVER_GOAL,
                turnOver.id,
                request.turnOverGoal.attachmentsList,
            )
            updateAttachments(
                AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE,
                turnOver.id,
                request.turnOverChallenge.attachmentsList,
            )
            updateAttachments(
                AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE,
                turnOver.id,
                request.turnOverRetrospective.attachmentsList,
            )

            turnOver
        } else {
            val turnOverGoal = TurnOverGoal(
                reason = request.turnOverGoal.reason,
                goal = request.turnOverGoal.goal,
            )

            // TurnOverChallenge는 필드가 없으므로 항상 새로 생성
            val turnOverChallenge = if (turnOver.turnOverChallenge == null) {
                TurnOverChallenge()
            } else {
                null
            }

            val turnOverRetrospective = TurnOverRetrospective(
                name = request.turnOverRetrospective.name,
                salary = request.turnOverRetrospective.salary,
                position = request.turnOverRetrospective.position,
                jobTitle = request.turnOverRetrospective.jobTitle,
                rank = request.turnOverRetrospective.rank,
                department = request.turnOverRetrospective.department,
                reason = request.turnOverRetrospective.reason,
                score = request.turnOverRetrospective.score,
                reviewSummary = request.turnOverRetrospective.reviewSummary,
                joinedAt = TimeUtil.ofEpochMilliNullable(request.turnOverRetrospective.joinedAt)?.toLocalDate(),
                workType = request.turnOverRetrospective.workType,
                employmentType = convertProtoEnumSafe<EmploymentType>(request.turnOverRetrospective.employmentType),
            )

            val turnOver = TurnOver(
                name = request.name,
                turnOverGoal = turnOverGoal,
                turnOverChallenge = turnOverChallenge,
                turnOverRetrospective = turnOverRetrospective,
                worker = worker,
                startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            )

            val savedTurnOver = turnOverRepository.save(turnOver)

            // Upsert collections
            upsertSelfIntroduction(savedTurnOver, request.turnOverGoal.selfIntroductionsList)
            upsertInterviewQuestion(savedTurnOver, request.turnOverGoal.interviewQuestionsList)
            upsertCheckList(savedTurnOver, request.turnOverGoal.checkListList)
            upsertJobApplication(savedTurnOver, request.turnOverChallenge.jobApplicationsList)
            upsertMemo(MemoTargetType.TURN_OVER_GOAL, savedTurnOver.id, request.turnOverGoal.memosList)
            upsertMemo(MemoTargetType.TURN_OVER_CHALLENGE, savedTurnOver.id, request.turnOverChallenge.memosList)
            upsertMemo(MemoTargetType.TURN_OVER_RETROSPECT, savedTurnOver.id, request.turnOverRetrospective.memosList)
            updateAttachments(
                AttachmentTargetType.ENTITY_TURN_OVER_GOAL,
                savedTurnOver.id,
                request.turnOverGoal.attachmentsList,
            )
            updateAttachments(
                AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE,
                savedTurnOver.id,
                request.turnOverChallenge.attachmentsList,
            )
            updateAttachments(
                AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE,
                savedTurnOver.id,
                request.turnOverRetrospective.attachmentsList,
            )

            savedTurnOver
        }

        turnOverRepository.save(turnOverEntity)
    }

    private fun upsertSelfIntroduction(
        turnOver: TurnOver,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOver.selfIntroductions.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existingMap[request.id]!!.apply {
                    changeInfo(
                        question = request.question,
                        content = request.content,
                        isVisible = request.isVisible,
                        priority = request.priority,
                    )
                }
            } else {
                // 새 엔티티 생성
                selfIntroductionService.createEntity(turnOver, request)
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOver.syncSelfIntroductions(updatedEntities)
    }

    private fun upsertInterviewQuestion(
        turnOver: TurnOver,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOver.interviewQuestions.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existingMap[request.id]!!.apply {
                    changeInfo(
                        question = request.question,
                        answer = request.answer,
                        isVisible = request.isVisible,
                        priority = request.priority,
                    )
                }
            } else {
                // 새 엔티티 생성
                interviewQuestionService.createEntity(turnOver, request)
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOver.syncInterviewQuestions(updatedEntities)
    }

    private fun upsertCheckList(
        turnOver: TurnOver,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOver.checkList.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existingMap[request.id]!!.apply {
                    changeInfo(
                        checked = request.checked,
                        content = request.content,
                        isVisible = request.isVisible,
                        priority = request.priority,
                    )
                }
            } else {
                // 새 엔티티 생성
                checkListService.createEntity(turnOver, request)
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOver.syncCheckLists(updatedEntities)
    }

    private fun upsertJobApplication(
        turnOver: TurnOver,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest>,
    ) {
        // 기존 엔티티 Map (ID로 빠른 조회)
        val existingMap = turnOver.jobApplications.associateBy { it.id }

        // 새로운 엔티티 리스트 생성
        val updatedEntities = requests.map { request ->
            if (request.hasId() && existingMap.containsKey(request.id)) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                val jobApplication = existingMap[request.id]!!.apply {
                    changeInfo(
                        name = request.name,
                        status = JobApplicationStatus.valueOf(request.status.name),
                        position = request.position,
                        jobPostingTitle = request.jobPostingTitle,
                        jobPostingUrl = request.jobPostingUrl,
                        isVisible = request.isVisible,
                        priority = request.priority,
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
                val jobApplication = jobApplicationService.createEntity(turnOver, request)
                // ApplicationStage도 Cascade로 처리
                upsertApplicationStage(jobApplication, request.applicationStagesList)
                jobApplication
            }
        }

        // Cascade를 통한 자동 처리 (삭제/추가)
        turnOver.syncJobApplications(updatedEntities)
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
                        isVisible = request.isVisible,
                        priority = request.priority,
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

    private fun upsertMemo(
        targetType: MemoTargetType,
        targetId: String,
        requests: List<TurnOverUpsertRequest.MemoRequest>,
    ) {
        val existingEntities = memoQueryService.listMemos(targetId, targetType)
        val existingIds = existingEntities.map { it.id }.toSet()
        val requestIds = requests.mapNotNull { it.id }.toSet()

        val toDelete = existingEntities.filter { it.id !in requestIds }
        val createMemos = requests.filter { !it.hasId() }
        val updateMemos = requests.filter { it.hasId() }

        memoCommandService.deleteMemos(toDelete.map { it.id })
        memoCommandService.createBulkMemo(targetType, targetId, createMemos)
        memoCommandService.updateBulkMemo(targetType, targetId, updateMemos)
    }

    private fun updateAttachments(
        targetType: AttachmentTargetType,
        targetId: String,
        attachmentRequests: List<AttachmentRequest>,
    ) {
        val storagePath = "turn-overs/attachments"
        val existingAttachments = attachmentQueryService.listAttachments(targetId, targetType)
        val existingIds = existingAttachments.map { it.id }.toSet()
        val requestIds = attachmentRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingAttachments.filter { it.id !in requestIds }
        val createRequests = attachmentRequests.filter { !it.hasId() }
        val updateRequests = attachmentRequests.filter { it.hasId() }

        attachmentCommandService.deleteAttachments(toDelete.map { it.id })
        attachmentCommandService.createBulkAttachment(targetType, targetId, storagePath, createRequests)
        attachmentCommandService.updateBulkAttachment(targetType, targetId, storagePath, updateRequests)
    }

    @Transactional(timeout = 60)  // 복제 작업은 여러 작업을 수행하므로 더 긴 타임아웃
    fun duplicate(id: String): TurnOver {
        // 1. 원본 TurnOver 조회
        val originalTurnOver = this.getTurnOver(id)

        // 2. Embedded 객체 복제
        val duplicatedTurnOverGoal = TurnOverGoal(
            reason = originalTurnOver.turnOverGoal.reason,
            goal = originalTurnOver.turnOverGoal.goal,
        )

        val duplicatedTurnOverChallenge = originalTurnOver.turnOverChallenge ?: TurnOverChallenge()

        val duplicatedTurnOverRetrospective = TurnOverRetrospective(
            name = originalTurnOver.turnOverRetrospective.name,
            salary = originalTurnOver.turnOverRetrospective.salary,
            position = originalTurnOver.turnOverRetrospective.position,
            jobTitle = originalTurnOver.turnOverRetrospective.jobTitle,
            rank = originalTurnOver.turnOverRetrospective.rank,
            department = originalTurnOver.turnOverRetrospective.department,
            reason = originalTurnOver.turnOverRetrospective.reason,
            score = originalTurnOver.turnOverRetrospective.score,
            reviewSummary = originalTurnOver.turnOverRetrospective.reviewSummary,
            joinedAt = originalTurnOver.turnOverRetrospective.joinedAt,
            workType = originalTurnOver.turnOverRetrospective.workType,
            employmentType = originalTurnOver.turnOverRetrospective.employmentType,
        )

        // 3. 새로운 TurnOver 생성
        val duplicatedTurnOver = TurnOver(
            name = "${originalTurnOver.name} (복제본)",
            turnOverGoal = duplicatedTurnOverGoal,
            turnOverChallenge = duplicatedTurnOverChallenge,
            turnOverRetrospective = duplicatedTurnOverRetrospective,
            worker = originalTurnOver.worker,
            startedAt = originalTurnOver.startedAt,
            endedAt = originalTurnOver.endedAt,
        )

        val savedTurnOver = turnOverRepository.save(duplicatedTurnOver)

        // 4. SelfIntroduction 복제
        originalTurnOver.selfIntroductions.forEach { originalSelfIntroduction ->
            selfIntroductionService.create(
                turnOver = savedTurnOver,
                request = TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest.newBuilder()
                    .setQuestion(originalSelfIntroduction.question)
                    .setContent(originalSelfIntroduction.content)
                    .setIsVisible(originalSelfIntroduction.isVisible)
                    .setPriority(originalSelfIntroduction.priority)
                    .build(),
            )
        }

        // 5. InterviewQuestion 복제
        originalTurnOver.interviewQuestions.forEach { originalInterviewQuestion ->
            interviewQuestionService.create(
                turnOver = savedTurnOver,
                request = TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest.newBuilder()
                    .setQuestion(originalInterviewQuestion.question)
                    .setAnswer(originalInterviewQuestion.answer)
                    .setIsVisible(originalInterviewQuestion.isVisible)
                    .setPriority(originalInterviewQuestion.priority)
                    .build(),
            )
        }

        // 6. CheckList 복제
        originalTurnOver.checkList.forEach { originalCheckList ->
            checkListService.create(
                turnOver = savedTurnOver,
                request = TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest.newBuilder()
                    .setChecked(originalCheckList.checked)
                    .setContent(originalCheckList.content)
                    .setIsVisible(originalCheckList.isVisible)
                    .setPriority(originalCheckList.priority)
                    .build(),
            )
        }

        // 7. JobApplication 복제 (ApplicationStage 포함)
        originalTurnOver.jobApplications.forEach { originalJobApplication ->
            val startedAtMillis = originalJobApplication.startedAt?.atStartOfDay()
                ?.let { TimeUtil.toEpochMilli(it) } ?: 0
            val endedAtMillis = originalJobApplication.endedAt?.atStartOfDay()
                ?.let { TimeUtil.toEpochMilli(it) } ?: 0

            val savedJobApplication = jobApplicationService.create(
                turnOver = savedTurnOver,
                request = TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.newBuilder()
                    .setName(originalJobApplication.name)
                    .setPosition(originalJobApplication.position)
                    .setJobPostingTitle(originalJobApplication.jobPostingTitle)
                    .setJobPostingUrl(originalJobApplication.jobPostingUrl)
                    .setStartedAt(startedAtMillis)
                    .setEndedAt(endedAtMillis)
                    .setApplicationSource(originalJobApplication.applicationSource)
                    .setMemo(originalJobApplication.memo)
                    .setStatus(
                        com.spectrum.workfolio.proto.common.JobApplication.JobApplicationStatus.valueOf(
                            originalJobApplication.status.name,
                        ),
                    )
                    .setIsVisible(originalJobApplication.isVisible)
                    .setPriority(originalJobApplication.priority)
                    .build(),
            )

            // ApplicationStage 복제
            originalJobApplication.applicationStages.forEach { originalStage ->
                applicationStageService.create(
                    jobApplication = savedJobApplication,
                    request = TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest.newBuilder()
                        .setName(originalStage.name)
                        .setStatus(
                            com.spectrum.workfolio.proto.common.ApplicationStage.ApplicationStageStatus.valueOf(
                                originalStage.status.name,
                            ),
                        )
                        .setStartedAt(originalStage.startedAt?.atStartOfDay()?.let { TimeUtil.toEpochMilli(it) } ?: 0)
                        .setMemo(originalStage.memo)
                        .setIsVisible(originalStage.isVisible)
                        .setPriority(originalStage.priority)
                        .build(),
                )
            }
        }

        // 8. Memo 복제 (Goal, Challenge, Retrospective 각각)
        // 8-1. TurnOverGoal Memo 복제
        val originalMemosGoal = memoQueryService.listMemos(originalTurnOver.id, MemoTargetType.TURN_OVER_GOAL)
        originalMemosGoal.forEach { originalMemo ->
            memoCommandService.createMemo(
                targetType = MemoTargetType.TURN_OVER_GOAL,
                targetId = savedTurnOver.id,
                request = TurnOverUpsertRequest.MemoRequest.newBuilder()
                    .setContent(originalMemo.content)
                    .setIsVisible(originalMemo.isVisible)
                    .setPriority(originalMemo.priority)
                    .build(),
            )
        }

        // 8-2. TurnOverChallenge Memo 복제
        val originalMemosChallenge = memoQueryService.listMemos(originalTurnOver.id, MemoTargetType.TURN_OVER_CHALLENGE)
        originalMemosChallenge.forEach { originalMemo ->
            memoCommandService.createMemo(
                targetType = MemoTargetType.TURN_OVER_CHALLENGE,
                targetId = savedTurnOver.id,
                request = TurnOverUpsertRequest.MemoRequest.newBuilder()
                    .setContent(originalMemo.content)
                    .setIsVisible(originalMemo.isVisible)
                    .setPriority(originalMemo.priority)
                    .build(),
            )
        }

        // 8-3. TurnOverRetrospective Memo 복제
        val originalMemosRetrospective =
            memoQueryService.listMemos(originalTurnOver.id, MemoTargetType.TURN_OVER_RETROSPECT)
        originalMemosRetrospective.forEach { originalMemo ->
            memoCommandService.createMemo(
                targetType = MemoTargetType.TURN_OVER_RETROSPECT,
                targetId = savedTurnOver.id,
                request = TurnOverUpsertRequest.MemoRequest.newBuilder()
                    .setContent(originalMemo.content)
                    .setIsVisible(originalMemo.isVisible)
                    .setPriority(originalMemo.priority)
                    .build(),
            )
        }

        // 9. Attachment 복제 (Goal, Challenge, Retrospective 각각)
        val allOriginalAttachments = attachmentQueryService.listAttachments(originalTurnOver.id)

        // 9-1. TurnOverGoal Attachment 복제
        val originalAttachmentsGoal = allOriginalAttachments.filter {
            it.targetType == AttachmentTargetType.ENTITY_TURN_OVER_GOAL
        }
        duplicateAttachments(
            originalAttachments = originalAttachmentsGoal,
            newTargetId = savedTurnOver.id,
            newTargetType = AttachmentTargetType.ENTITY_TURN_OVER_GOAL,
        )

        // 9-2. TurnOverChallenge Attachment 복제
        val originalAttachmentsChallenge = allOriginalAttachments.filter {
            it.targetType == AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE
        }
        duplicateAttachments(
            originalAttachments = originalAttachmentsChallenge,
            newTargetId = savedTurnOver.id,
            newTargetType = AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE,
        )

        // 9-3. TurnOverRetrospective Attachment 복제
        val originalAttachmentsRetrospective = allOriginalAttachments.filter {
            it.targetType == AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE
        }
        duplicateAttachments(
            originalAttachments = originalAttachmentsRetrospective,
            newTargetId = savedTurnOver.id,
            newTargetType = AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE,
        )

        return savedTurnOver
    }

    private fun duplicateAttachments(
        originalAttachments: List<com.spectrum.workfolio.domain.entity.common.Attachment>,
        newTargetId: String,
        newTargetType: AttachmentTargetType,
    ) {
        originalAttachments.forEach { originalAttachment ->
            val newAttachment = com.spectrum.workfolio.domain.entity.common.Attachment(
                fileName = originalAttachment.fileName,
                fileUrl = "",
                url = originalAttachment.url,
                isVisible = originalAttachment.isVisible,
                priority = originalAttachment.priority,
                type = originalAttachment.type,
                category = originalAttachment.category,
                targetId = newTargetId,
                targetType = newTargetType,
            )
            val savedAttachment = attachmentRepository.save(newAttachment)

            // 파일이 있으면 복사
            if (originalAttachment.fileUrl.isNotBlank()) {
                try {
                    val extension = FileUtil.extractFileExtension(originalAttachment.fileName)
                    val newFileName = "${savedAttachment.id}.$extension"

                    val copiedFileUrl = fileUploadService.copyFileInStorage(
                        sourceFileUrl = originalAttachment.fileUrl,
                        destinationFileName = newFileName,
                        destinationStoragePath = getStoragePath(newTargetType, newTargetId),
                    )

                    savedAttachment.changeFileUrl(copiedFileUrl)
                } catch (e: Exception) {
                    logger.warn("Failed to copy file for attachment: ${originalAttachment.id}", e)
                }
            }
        }
    }

    private fun getStoragePath(targetType: AttachmentTargetType, targetId: String): String {
        return when (targetType) {
            AttachmentTargetType.ENTITY_TURN_OVER_GOAL -> "turn_over/goals/$targetId"
            AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE -> "turn_over/challenges/$targetId"
            AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE -> "turn_over/retrospectives/$targetId"
            else -> "turn_over/$targetId"
        }
    }

    @Transactional  // 전역 타임아웃(30초) 적용
    fun delete(id: String) {
        turnOverRepository.deleteById(id)
    }
}
