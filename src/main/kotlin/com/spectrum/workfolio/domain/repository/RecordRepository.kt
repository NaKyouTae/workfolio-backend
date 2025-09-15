package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.record.Record
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RecordRepository: JpaRepository<Record, String> {
    fun findAllByWorker(worker: Worker): List<Record>
    // BETWEEN 사용 (권장)
    @Query("SELECT r FROM Record r WHERE r.worker = :worker AND r.startedAt >= :startDate AND r.startedAt < :endDate")
    fun findByDateRange(@Param("worker") worker: Worker, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Record>

}
