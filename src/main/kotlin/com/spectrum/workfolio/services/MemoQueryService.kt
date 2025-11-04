package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.common.Memo
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.MemoRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemoQueryService(
    private val memoRepository: MemoRepository,
) {
    @Transactional(readOnly = true)
    fun getMemo(id: String): Memo {
        return memoRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_MEMO.message)
        }
    }

    @Transactional(readOnly = true)
    fun listMemos(targetId: String): List<Memo> {
        return memoRepository.findByTargetId(targetId)
    }

    @Transactional(readOnly = true)
    fun listMemos(targetIds: List<String>): List<Memo> {
        return memoRepository.findByTargetIdIn(targetIds)
    }
}
