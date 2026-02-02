package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.payments.CreditHistory
import com.spectrum.workfolio.domain.enums.CreditTxType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CreditHistoryRepository : JpaRepository<CreditHistory, String> {
    fun findByWorkerIdOrderByCreatedAtDesc(workerId: String, pageable: Pageable): Page<CreditHistory>
    fun findByWorkerIdAndTxTypeOrderByCreatedAtDesc(workerId: String, txType: CreditTxType, pageable: Pageable): Page<CreditHistory>
}
