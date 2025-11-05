package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.common.Memo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemoRepository : JpaRepository<Memo, String> {
    fun findByTargetIdOrderByPriorityAsc(targetId: String): List<Memo>
    fun findByTargetIdInOrderByPriorityAsc(targetIds: List<String>): List<Memo>
}
