package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.payments.CreditHistory
import com.spectrum.workfolio.domain.enums.CreditTxType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CreditHistoryRepository : JpaRepository<CreditHistory, String> {
    fun findByWorkerIdOrderByCreatedAtDesc(workerId: String, pageable: Pageable): Page<CreditHistory>
    fun findByWorkerIdAndTxTypeOrderByCreatedAtDesc(workerId: String, txType: CreditTxType, pageable: Pageable): Page<CreditHistory>
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<CreditHistory>
    fun findByTxTypeOrderByCreatedAtDesc(txType: CreditTxType, pageable: Pageable): Page<CreditHistory>

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CreditHistory c WHERE c.txType IN :txTypes")
    fun sumAmountByTxTypes(@Param("txTypes") txTypes: List<CreditTxType>): Long
}
