package com.spectrum.workfolio.services.turnovers

import com.spectrum.workfolio.domain.entity.turnover.TurnOverChallenge
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.repository.TurnOverChallengeRepository
import com.spectrum.workfolio.proto.common.TurnOverChallengeDetail
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import com.spectrum.workfolio.services.AttachmentQueryService
import com.spectrum.workfolio.services.MemoQueryService
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TurnOverChallengeService(
    private val memoQueryService: MemoQueryService,
    private val attachmentQueryService: AttachmentQueryService,
    private val turnOverChallengeRepository: TurnOverChallengeRepository,
) {
    @Transactional(readOnly = true)
    fun getTurnOverChallenge(id: String): TurnOverChallenge {
        return turnOverChallengeRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_TURN_OVER_CHALLENGE.message)
        }
    }

    @Transactional(readOnly = true)
    fun getTurnOverChallengeDetail(id: String): TurnOverChallengeDetail {
        val turnOverChallenge = this.getTurnOverChallenge(id)
        val memos = memoQueryService.listMemos(turnOverChallenge.id)
        val attachments = attachmentQueryService.listAttachments(turnOverChallenge.id)
        return turnOverChallenge.toDetailProto(memos, attachments)
    }

    @Transactional
    fun create(request: TurnOverUpsertRequest.TurnOverChallengeRequest): TurnOverChallenge {
        val turnOverChallenge = TurnOverChallenge()

        return turnOverChallengeRepository.save(turnOverChallenge)
    }

    @Transactional
    fun update(request: TurnOverUpsertRequest.TurnOverChallengeRequest): TurnOverChallenge {
        val turnOverChallenge = this.getTurnOverChallenge(request.id)

        return turnOverChallengeRepository.save(turnOverChallenge)
    }
}
