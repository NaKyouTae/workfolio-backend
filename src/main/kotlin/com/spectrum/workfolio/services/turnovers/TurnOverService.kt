package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import com.spectrum.workfolio.domain.enums.ApplicationStageStatus
import com.spectrum.workfolio.domain.enums.AttachmentTargetType
import com.spectrum.workfolio.domain.enums.JobApplicationStatus
import com.spectrum.workfolio.domain.enums.MemoTargetType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.TurnOverRepository
import com.spectrum.workfolio.proto.attachment.AttachmentRequest
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
    private val turnOverGoalService: TurnOverGoalService,
    private val attachmentRepository: com.spectrum.workfolio.domain.repository.AttachmentRepository,
    private val turnOverGoalRepository: com.spectrum.workfolio.domain.repository.TurnOverGoalRepository,
    private val turnOverChallengeRepository: com.spectrum.workfolio.domain.repository.TurnOverChallengeRepository,
    private val jobApplicationService: JobApplicationService,
    private val attachmentQueryService: AttachmentQueryService,
    private val applicationStageService: ApplicationStageService,
    private val selfIntroductionService: SelfIntroductionService,
    private val attachmentCommandService: AttachmentCommandService,
    private val interviewQuestionService: InterviewQuestionService,
    private val turnOverChallengeService: TurnOverChallengeService,
    private val turnOverRetrospectiveService: TurnOverRetrospectiveService,
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

    @Transactional(readOnly = true)
    fun listTurnOverDetailsResult(workerId: String): TurnOverDetailListResponse {
        val turnOvers = turnOverRepository.findByWorkerId(workerId)
        val turnOversResult = turnOvers.map {
            val turnOverGoal = turnOverGoalService.getTurnOverGoalDetail(it.turnOverGoal.id)
            val turnOverChallenge = turnOverChallengeService.getTurnOverChallengeDetail(it.turnOverChallenge.id)
            val turnOverRetrospective = turnOverRetrospectiveService.getTurnOverRetrospectiveDetail(it.turnOverRetrospective.id)

            it.toDetailProto(turnOverGoal, turnOverChallenge, turnOverRetrospective)
        }

        return TurnOverDetailListResponse.newBuilder()
            .addAllTurnOvers(turnOversResult)
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
        updateAttachments(AttachmentTargetType.ENTITY_TURN_OVER_GOAL, turnOverGoal.id, request.attachmentsList)

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
                        isVisible = request.isVisible,
                        priority = request.priority,
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
                        isVisible = request.isVisible,
                        priority = request.priority,
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
                        isVisible = request.isVisible,
                        priority = request.priority,
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
        updateAttachments(
            AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE,
            turnOverChallenge.id,
            request.attachmentsList,
        )

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

    private fun upsertTurnOverRetrospective(request: TurnOverUpsertRequest.TurnOverRetrospectiveRequest): TurnOverRetrospective {
        val turnOverRetrospective = if (request.hasId()) {
            turnOverRetrospectiveService.update(request)
        } else {
            turnOverRetrospectiveService.create(request)
        }

        upsertMemo(MemoTargetType.TURN_OVER_RETROSPECT, turnOverRetrospective.id, request.memosList)
        updateAttachments(
            AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE,
            turnOverRetrospective.id,
            request.attachmentsList,
        )

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
        targetType: AttachmentTargetType,
        targetId: String,
        attachmentRequests: List<AttachmentRequest>,
    ) {
        val storagePath = "turn-overs/attachments"
        val existingAttachments = attachmentQueryService.listAttachments(targetId)
        val existingIds = existingAttachments.map { it.id }.toSet()
        val requestIds = attachmentRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingAttachments.filter { it.id !in requestIds }
        val createRequests = attachmentRequests.filter { !it.hasId() }
        val updateRequests = attachmentRequests.filter { it.hasId() }

        attachmentCommandService.deleteAttachments(toDelete.map { it.id })
        attachmentCommandService.createBulkAttachment(targetType, targetId, storagePath, createRequests)
        attachmentCommandService.updateBulkAttachment(targetId, storagePath, updateRequests)
    }

    @Transactional
    fun duplicate(id: String): TurnOver {
        // 1. 원본 TurnOver 조회
        val originalTurnOver = this.getTurnOver(id)

        // 2. TurnOverGoal 복제
        val duplicatedTurnOverGoal = duplicateTurnOverGoal(originalTurnOver.turnOverGoal)

        // 3. TurnOverChallenge 복제
        val duplicatedTurnOverChallenge = duplicateTurnOverChallenge(originalTurnOver.turnOverChallenge)

        // 4. TurnOverRetrospective 복제
        val duplicatedTurnOverRetrospective = duplicateTurnOverRetrospective(originalTurnOver.turnOverRetrospective)

        // 5. 새로운 TurnOver 생성
        val duplicatedTurnOver = TurnOver(
            name = "${originalTurnOver.name} (복제본)",
            turnOverGoal = duplicatedTurnOverGoal,
            turnOverChallenge = duplicatedTurnOverChallenge,
            turnOverRetrospective = duplicatedTurnOverRetrospective,
            worker = originalTurnOver.worker,
        )

        return turnOverRepository.save(duplicatedTurnOver)
    }

    private fun duplicateTurnOverGoal(originalTurnOverGoal: TurnOverGoal): TurnOverGoal {
        // 1. 새로운 TurnOverGoal 생성
        val duplicatedTurnOverGoal = TurnOverGoal(
            reason = originalTurnOverGoal.reason,
            goal = originalTurnOverGoal.goal,
        )
        val savedTurnOverGoal = turnOverGoalRepository.save(duplicatedTurnOverGoal)

        // 2. SelfIntroduction 복제
        originalTurnOverGoal.selfIntroductions.forEach { originalSelfIntroduction ->
            selfIntroductionService.create(
                turnOverGoal = savedTurnOverGoal,
                request = TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest.newBuilder()
                    .setQuestion(originalSelfIntroduction.question)
                    .setContent(originalSelfIntroduction.content)
                    .build(),
            )
        }

        // 3. InterviewQuestion 복제
        originalTurnOverGoal.interviewQuestions.forEach { originalInterviewQuestion ->
            interviewQuestionService.create(
                turnOverGoal = savedTurnOverGoal,
                request = TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest.newBuilder()
                    .setQuestion(originalInterviewQuestion.question)
                    .setAnswer(originalInterviewQuestion.answer)
                    .build(),
            )
        }

        // 4. CheckList 복제
        originalTurnOverGoal.checkList.forEach { originalCheckList ->
            checkListService.create(
                turnOverGoal = savedTurnOverGoal,
                request = TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest.newBuilder()
                    .setChecked(originalCheckList.checked)
                    .setContent(originalCheckList.content)
                    .build(),
            )
        }

        // 5. Memo 복제
        val originalMemos = memoQueryService.listMemos(originalTurnOverGoal.id)
        originalMemos.forEach { originalMemo ->
            memoCommandService.createMemo(
                targetType = MemoTargetType.TURN_OVER_GOAL,
                targetId = savedTurnOverGoal.id,
                request = TurnOverUpsertRequest.MemoRequest.newBuilder()
                    .setContent(originalMemo.content)
                    .build(),
            )
        }

        // 6. Attachment 복제
        val originalAttachments = attachmentQueryService.listAttachments(originalTurnOverGoal.id)
        duplicateAttachments(
            originalAttachments = originalAttachments,
            newTargetId = savedTurnOverGoal.id,
            newTargetType = AttachmentTargetType.ENTITY_TURN_OVER_GOAL,
        )

        return savedTurnOverGoal
    }

    private fun duplicateTurnOverChallenge(originalTurnOverChallenge: TurnOverChallenge): TurnOverChallenge {
        // 1. 새로운 TurnOverChallenge 생성
        val duplicatedTurnOverChallenge = TurnOverChallenge()
        val savedTurnOverChallenge = turnOverChallengeRepository.save(duplicatedTurnOverChallenge)

        // 2. JobApplication 복제 (ApplicationStage 포함)
        originalTurnOverChallenge.jobApplications.forEach { originalJobApplication ->
            val startedAtMillis = originalJobApplication.startedAt?.atStartOfDay()
                ?.let { TimeUtil.toEpochMilli(it) } ?: 0
            val endedAtMillis = originalJobApplication.endedAt?.atStartOfDay()
                ?.let { TimeUtil.toEpochMilli(it) } ?: 0

            val savedJobApplication = jobApplicationService.create(
                turnOverChallenge = savedTurnOverChallenge,
                request = TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.newBuilder()
                    .setName(originalJobApplication.name)
                    .setPosition(originalJobApplication.position)
                    .setJobPostingTitle(originalJobApplication.jobPostingTitle)
                    .setJobPostingUrl(originalJobApplication.jobPostingUrl)
                    .setStartedAt(startedAtMillis)
                    .setEndedAt(endedAtMillis)
                    .setApplicationSource(originalJobApplication.applicationSource)
                    .setMemo(originalJobApplication.memo)
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
                        .build(),
                )
            }
        }

        // 3. Memo 복제
        val originalMemos = memoQueryService.listMemos(originalTurnOverChallenge.id)
        originalMemos.forEach { originalMemo ->
            memoCommandService.createMemo(
                targetType = MemoTargetType.TURN_OVER_CHALLENGE,
                targetId = savedTurnOverChallenge.id,
                request = TurnOverUpsertRequest.MemoRequest.newBuilder()
                    .setContent(originalMemo.content)
                    .build(),
            )
        }

        // 4. Attachment 복제
        val originalAttachments = attachmentQueryService.listAttachments(originalTurnOverChallenge.id)
        duplicateAttachments(
            originalAttachments = originalAttachments,
            newTargetId = savedTurnOverChallenge.id,
            newTargetType = AttachmentTargetType.ENTITY_TURN_OVER_CHALLENGE,
        )

        return savedTurnOverChallenge
    }

    private fun duplicateTurnOverRetrospective(originalTurnOverRetrospective: TurnOverRetrospective): TurnOverRetrospective {
        // 1. 새로운 TurnOverRetrospective 생성
        val joinedAtMillis = originalTurnOverRetrospective.joinedAt?.atStartOfDay()
            ?.let { TimeUtil.toEpochMilli(it) } ?: 0

        val duplicatedTurnOverRetrospective = turnOverRetrospectiveService.create(
            request = TurnOverUpsertRequest.TurnOverRetrospectiveRequest.newBuilder()
                .setName(originalTurnOverRetrospective.name)
                .setSalary(originalTurnOverRetrospective.salary)
                .setPosition(originalTurnOverRetrospective.position)
                .setJobTitle(originalTurnOverRetrospective.jobTitle)
                .setRank(originalTurnOverRetrospective.rank)
                .setDepartment(originalTurnOverRetrospective.department)
                .setReason(originalTurnOverRetrospective.reason)
                .setScore(originalTurnOverRetrospective.score)
                .setReviewSummary(originalTurnOverRetrospective.reviewSummary)
                .setJoinedAt(joinedAtMillis)
                .setWorkType(originalTurnOverRetrospective.workType)
                .setEmploymentType(
                    com.spectrum.workfolio.proto.common.TurnOverRetrospective.EmploymentType.valueOf(
                        originalTurnOverRetrospective.employmentType?.name ?: "EMPLOYMENT_TYPE_UNKNOWN",
                    ),
                )
                .build(),
        )

        // 2. Memo 복제
        val originalMemos = memoQueryService.listMemos(originalTurnOverRetrospective.id)
        originalMemos.forEach { originalMemo ->
            memoCommandService.createMemo(
                targetType = MemoTargetType.TURN_OVER_RETROSPECT,
                targetId = duplicatedTurnOverRetrospective.id,
                request = TurnOverUpsertRequest.MemoRequest.newBuilder()
                    .setContent(originalMemo.content)
                    .build(),
            )
        }

        // 3. Attachment 복제
        val originalAttachments = attachmentQueryService.listAttachments(originalTurnOverRetrospective.id)
        duplicateAttachments(
            originalAttachments = originalAttachments,
            newTargetId = duplicatedTurnOverRetrospective.id,
            newTargetType = AttachmentTargetType.ENTITY_TURN_OVER_RETROSPECTIVE,
        )

        return duplicatedTurnOverRetrospective
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

    @Transactional
    fun delete(id: String) {
        turnOverRepository.deleteById(id)
    }
}
