package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.common.Notice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoticeRepository : JpaRepository<Notice, String> {
    fun findAllByOrderByIsPinnedDescCreatedAtDesc(): List<Notice>
}

