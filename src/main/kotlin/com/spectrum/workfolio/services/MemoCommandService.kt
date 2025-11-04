package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.common.Memo
import com.spectrum.workfolio.domain.enums.MemoTargetType
import com.spectrum.workfolio.domain.repository.MemoRepository
import com.spectrum.workfolio.proto.turn_over.TurnOverUpsertRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemoCommandService(
    private val memoRepository: MemoRepository,
    private val memoQueryService: MemoQueryService,
) {

    @Transactional
    fun createMemo(targetType: MemoTargetType, targetId: String, request: TurnOverUpsertRequest.MemoRequest): Memo {
        val memo = Memo(
            content = request.content,
            targetType = targetType,
            targetId = targetId,
        )

        return memoRepository.save(memo)
    }

    @Transactional
    fun createBulkMemo(
        targetType: MemoTargetType,
        targetId: String,
        requests: List<TurnOverUpsertRequest.MemoRequest>,
    ) {
        val entities = requests.map { memo ->
            Memo(
                content = memo.content,
                targetType = targetType,
                targetId = targetId,
            )
        }

        memoRepository.saveAll(entities)
    }

    @Transactional
    fun updateMemo(request: TurnOverUpsertRequest.MemoRequest): Memo {
        val memo = memoQueryService.getMemo(request.id)

        memo.changeInfo(
            content = request.content,
        )

        return memoRepository.save(memo)
    }

    @Transactional
    fun updateBulkMemo(targetId: String, requests: List<TurnOverUpsertRequest.MemoRequest>): List<Memo> {
        // 1. targetId로 기존 memos 조회
        val existingMemos = memoQueryService.listMemos(targetId)

        // 2. 파라미터로 받은 requests를 ID로 Map 구조로 변환 (빠른 조회를 위해)
        val requestMap = requests
            .filter { it.id.isNotBlank() } // ID가 있는 것만 (업데이트 대상)
            .associateBy { it.id }

        // 3. 기존 memos를 순회하면서 일치하는 request를 찾아 changeInfo 호출
        val updatedMemos = existingMemos.mapNotNull { memo ->
            requestMap[memo.id]?.let { request ->
                memo.changeInfo(
                    content = request.content,
                )
                memo
            }
        }

        // 4. saveAll을 이용해서 한번에 업데이트
        return memoRepository.saveAll(updatedMemos)
    }

    @Transactional
    fun deleteMemos(ids: List<String>) {
        if (ids.isNotEmpty()) {
            memoRepository.deleteAllById(ids)
        }
    }
}
