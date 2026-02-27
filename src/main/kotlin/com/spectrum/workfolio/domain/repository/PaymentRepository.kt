package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.payments.Payment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface PaymentRepository : JpaRepository<Payment, String> {
    fun findByWorkerIdOrderByCreatedAtDesc(workerId: String, pageable: Pageable): Page<Payment>
    fun findByWorkerIdAndStatusOrderByCreatedAtDesc(workerId: String, status: String, pageable: Pageable): Page<Payment>
    fun findByStatusOrderByCreatedAtDesc(status: String, pageable: Pageable): Page<Payment>

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status IN :statuses")
    fun sumAmountByStatuses(@Param("statuses") statuses: List<String>): BigDecimal
}
