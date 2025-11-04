package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import com.spectrum.workfolio.domain.enums.EmploymentType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.repository.TurnOverRetrospectiveRepository
import com.spectrum.workfolio.proto.common.TurnOverRetrospectiveDetail
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.services.AttachmentQueryService
import com.spectrum.workfolio.services.MemoQueryService
import com.spectrum.workfolio.utils.EnumUtils.convertProtoEnumSafe
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TurnOverRetrospectiveService(
    private val memoQueryService: MemoQueryService,
    private val attachmentQueryService: AttachmentQueryService,
    private val turnOverRetrospectiveRepository: TurnOverRetrospectiveRepository,
) {
    @Transactional(readOnly = true)
    fun getTurnOverRetrospective(id: String): TurnOverRetrospective {
        return turnOverRetrospectiveRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_TURN_OVER_RETROSPECTIVE.message)
        }
    }

    @Transactional(readOnly = true)
    fun getTurnOverRetrospectiveDetail(id: String): TurnOverRetrospectiveDetail {
        val turnOverRetrospective = this.getTurnOverRetrospective(id)
        val memos = memoQueryService.listMemos(turnOverRetrospective.id)
        val attachments = attachmentQueryService.listAttachments(turnOverRetrospective.id)
        return turnOverRetrospective.toDetailProto(memos, attachments)
    }

    @Transactional
    fun create(request: TurnOverUpsertRequest.TurnOverRetrospectiveRequest): TurnOverRetrospective {
        val turnOverRetrospective = TurnOverRetrospective(
            name = request.name,
            salary = request.salary,
            position = request.position,
            jobTitle = request.jobTitle,
            rank = request.rank,
            department = request.department,
            reason = request.reason,
            score = request.score,
            reviewSummary = request.reviewSummary,
            joinedAt = TimeUtil.ofEpochMilliNullable(request.joinedAt)?.toLocalDate(),
            workType = request.workType,
            employmentType = convertProtoEnumSafe<EmploymentType>(request.employmentType),
        )

        return turnOverRetrospectiveRepository.save(turnOverRetrospective)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverRetrospectiveRequest): TurnOverRetrospective {
        val turnOverRetrospective = this.getTurnOverRetrospective(request.id)

        turnOverRetrospective.changeInfo(
            name = request.name,
            salary = request.salary,
            position = request.position,
            jobTitle = request.jobTitle,
            rank = request.rank,
            department = request.department,
            reason = request.reason,
            score = request.score,
            reviewSummary = request.reviewSummary,
            joinedAt = TimeUtil.ofEpochMilliNullable(request.joinedAt)?.toLocalDate(),
            workType = request.workType,
            employmentType = convertProtoEnumSafe<EmploymentType>(request.employmentType),
        )

        return turnOverRetrospectiveRepository.save(turnOverRetrospective)
    }
}
