package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.common.NoticeRead
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NoticeReadRepository : JpaRepository<NoticeRead, String> {
    fun existsByWorkerIdAndNoticeId(workerId: String, noticeId: String): Boolean

    @Query("SELECT nr.notice.id FROM NoticeRead nr WHERE nr.worker.id = :workerId")
    fun findReadNoticeIdsByWorkerId(workerId: String): List<String>

    fun deleteAllByWorkerId(workerId: String)
}
