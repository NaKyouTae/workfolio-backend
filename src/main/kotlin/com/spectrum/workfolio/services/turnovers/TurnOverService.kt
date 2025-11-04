package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.JobApplication
import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.domain.entity.turnover.TurnOverGoal
import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import com.spectrum.workfolio.domain.enums.MemoTargetType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.repository.TurnOverRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverDetailListResponse
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.services.MemoCommandService
import com.spectrum.workfolio.services.MemoQueryService
import com.spectrum.workfolio.services.WorkerService
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
    private val applicationStageService: ApplicationStageService,
    private val selfIntroductionService: SelfIntroductionService,
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
    fun listTurnOversResult(workerId: String): TurnOverDetailListResponse {
        val turnOvers = turnOverRepository.findByWorkerId(workerId)
        return TurnOverDetailListResponse.newBuilder()
            .addAllTurnOvers(turnOvers.map { it.toDetailProto() })
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

        return turnOverGoal
    }

    private fun upsertSelfIntroduction(
        turnOverGoal: TurnOverGoal,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.SelfIntroductionRequest>,
    ) {
        val existingEntities = selfIntroductionService.getSelfIntroductions(turnOverGoal.id)
        val requestIds = requests.mapNotNull { it.id }.toSet()

        val createEntities = requests.filter { !it.hasId() }
        val updateEntities = requests.filter { it.hasId() }
        val toDelete = existingEntities.filter { it.id !in requestIds }

        selfIntroductionService.deleteSelfIntroductions(toDelete.map { it.id })
        selfIntroductionService.createBulk(turnOverGoal, createEntities)
        selfIntroductionService.updateBulk(turnOverGoal.id, updateEntities)
    }

    private fun upsertInterviewQuestion(
        turnOverGoal: TurnOverGoal,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.InterviewQuestionRequest>,
    ) {
        val existingEntities = interviewQuestionService.getInterviewQuestions(turnOverGoal.id)
        val requestIds = requests.mapNotNull { it.id }.toSet()

        val createEntities = requests.filter { !it.hasId() }
        val updateEntities = requests.filter { it.hasId() }
        val toDelete = existingEntities.filter { it.id !in requestIds }

        interviewQuestionService.deleteInterviewQuestions(toDelete.map { it.id })
        interviewQuestionService.createBulk(turnOverGoal, createEntities)
        interviewQuestionService.updateBulk(turnOverGoal.id, updateEntities)
    }

    private fun upsertCheckList(
        turnOverGoal: TurnOverGoal,
        requests: List<TurnOverUpsertRequest.TurnOverGoalRequest.CheckListRequest>,
    ) {
        val existingCheckList = checkListService.getCheckLists(turnOverGoal.id)
        val requestIds = requests.mapNotNull { it.id }.toSet()

        val createEntities = requests.filter { !it.hasId() }
        val updateEntities = requests.filter { it.hasId() }
        val toDelete = existingCheckList.filter { it.id !in requestIds }

        checkListService.deleteCheckLists(toDelete.map { it.id })
        checkListService.create(turnOverGoal, createEntities)
        checkListService.updateBulk(turnOverGoal.id, updateEntities)
    }

    private fun upsertTurnOverChallenge(request: TurnOverUpsertRequest.TurnOverChallengeRequest): TurnOverChallenge {
        val turnOverChallenge = if (request.hasId()) {
            turnOverChallengeService.update(request)
        } else {
            turnOverChallengeService.create(request)
        }

        upsertJobApplication(turnOverChallenge, request.jobApplicationsList)
        upsertMemo(MemoTargetType.TURN_OVER_CHALLENGE, turnOverChallenge.id, request.memosList)

        return turnOverChallenge
    }

    private fun upsertJobApplication(
        turnOverChallenge: TurnOverChallenge,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest>,
    ) {
        val existingJobApplications = jobApplicationService.getJobApplications(turnOverChallenge.id)
        val requestIds = requests.mapNotNull { it.id }.toSet()

        val createEntities = requests.filter { !it.hasId() }
        val updateEntities = requests.filter { it.hasId() }
        val toDelete = existingJobApplications.filter { it.id !in requestIds }

        jobApplicationService.deleteJobApplications(toDelete.map { it.id })

        // 생성된 엔티티 처리
        val createdJobApplications = jobApplicationService.createBulk(turnOverChallenge, createEntities)
        createdJobApplications.forEachIndexed { index, jobApplication ->
            upsertApplicationStage(jobApplication, createEntities[index].applicationStagesList)
        }

        // 업데이트된 엔티티 처리
        val updatedJobApplications = jobApplicationService.updateBulk(turnOverChallenge.id, updateEntities)
        updatedJobApplications.forEachIndexed { index, jobApplication ->
            upsertApplicationStage(jobApplication, updateEntities[index].applicationStagesList)
        }
    }

    private fun upsertApplicationStage(
        jobApplication: JobApplication,
        requests: List<TurnOverUpsertRequest.TurnOverChallengeRequest.JobApplicationRequest.ApplicationStageRequest>,
    ) {
        val existingApplicationStages = applicationStageService.getApplicationStages(jobApplication.id)
        val requestIds = requests.mapNotNull { it.id }.toSet()

        val createEntities = requests.filter { !it.hasId() }
        val updateEntities = requests.filter { it.hasId() }
        val toDelete = existingApplicationStages.filter { it.id !in requestIds }

        applicationStageService.deleteApplicationStages(toDelete.map { it.id })
        applicationStageService.createBulk(jobApplication, createEntities)
        applicationStageService.updateBulk(jobApplication.id, updateEntities)
    }

    private fun upsertTurnOverRetrospective(request: TurnOverUpsertRequest.TurnOverRetrospectiveRequest): TurnOverRetrospective {
        val turnOverRetrospective = if (request.hasId()) {
            turnOverRetrospectiveService.update(request)
        } else {
            turnOverRetrospectiveService.create(request)
        }

        upsertMemo(MemoTargetType.TURN_OVER_RETROSPECT, turnOverRetrospective.id, request.memosList)

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
}
