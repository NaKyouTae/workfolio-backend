package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.common.Memo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemoRepository : JpaRepository<Memo, String> {
    fun findByTargetId(targetId: String): List<Memo>
    fun findByTargetIdIn(targetIds: List<String>): List<Memo>
}
